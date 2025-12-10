package com.example.customerservice.service;

import com.example.customerservice.dto.request.AddressRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressNormalizationService {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${address.lookup.base-url:https://provinces.open-api.vn/api}")
    private String baseUrl;

    private volatile RestTemplate restTemplate;

    private final Map<String, ProvincePayload> provinceCache = new ConcurrentHashMap<>();
    private final Map<String, DistrictPayload> districtCache = new ConcurrentHashMap<>();
    private final Map<String, WardPayload> wardCache = new ConcurrentHashMap<>();

    public AddressRequest normalize(AddressRequest request) {
        if (request == null) {
            return null;
        }

        String street = trim(request.getStreet());
        String wardValue = trim(request.getWard());
        String districtValue = trim(request.getDistrict());
        String cityValue = trim(request.getCity());
        String country = trim(request.getCountry());

        ProvincePayload provincePayload = fetchProvinceIfCode(cityValue);
        DistrictPayload districtPayload = fetchDistrictIfCode(districtValue);
        WardPayload wardPayload = fetchWardIfCode(wardValue);

        String resolvedWard = resolveWardName(wardValue, wardPayload);
        String resolvedDistrict = resolveDistrictName(districtValue, districtPayload, wardPayload);
        String resolvedCity = resolveProvinceName(cityValue, provincePayload, districtPayload, wardPayload);

        return AddressRequest.builder()
                .street(street)
                .ward(resolvedWard)
                .district(resolvedDistrict)
                .city(resolvedCity)
                .country(country)
                .build();
    }

    private String resolveWardName(String original, WardPayload wardPayload) {
        if (wardPayload != null && StringUtils.hasText(wardPayload.getName())) {
            return wardPayload.getName();
        }
        return original;
    }

    private String resolveDistrictName(String original,
                                       DistrictPayload districtPayload,
                                       WardPayload wardPayload) {
        if (districtPayload != null && StringUtils.hasText(districtPayload.getName())) {
            return districtPayload.getName();
        }
        if (wardPayload != null && StringUtils.hasText(wardPayload.getDistrictCode())) {
            DistrictPayload fromWard = getDistrictByCode(wardPayload.getDistrictCode());
            if (fromWard != null && StringUtils.hasText(fromWard.getName())) {
                return fromWard.getName();
            }
        }
        return original;
    }

    private String resolveProvinceName(String original,
                                       ProvincePayload provincePayload,
                                       DistrictPayload districtPayload,
                                       WardPayload wardPayload) {
        if (provincePayload != null && StringUtils.hasText(provincePayload.getName())) {
            return provincePayload.getName();
        }
        if (districtPayload != null && StringUtils.hasText(districtPayload.getProvinceCode())) {
            ProvincePayload fromDistrict = getProvinceByCode(districtPayload.getProvinceCode());
            if (fromDistrict != null && StringUtils.hasText(fromDistrict.getName())) {
                return fromDistrict.getName();
            }
        }
        if (wardPayload != null && StringUtils.hasText(wardPayload.getDistrictCode())) {
            DistrictPayload fromWard = getDistrictByCode(wardPayload.getDistrictCode());
            if (fromWard != null && StringUtils.hasText(fromWard.getProvinceCode())) {
                ProvincePayload fromWardProvince = getProvinceByCode(fromWard.getProvinceCode());
                if (fromWardProvince != null && StringUtils.hasText(fromWardProvince.getName())) {
                    return fromWardProvince.getName();
                }
            }
        }
        return original;
    }

    private ProvincePayload fetchProvinceIfCode(String value) {
        if (!isLikelyCode(value)) {
            return null;
        }
        return getProvinceByCode(value);
    }

    private DistrictPayload fetchDistrictIfCode(String value) {
        if (!isLikelyCode(value)) {
            return null;
        }
        return getDistrictByCode(value);
    }

    private WardPayload fetchWardIfCode(String value) {
        if (!isLikelyCode(value)) {
            return null;
        }
        return getWardByCode(value);
    }

    private ProvincePayload getProvinceByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return provinceCache.computeIfAbsent(code, this::fetchProvince);
    }

    private DistrictPayload getDistrictByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return districtCache.computeIfAbsent(code, this::fetchDistrict);
    }

    private WardPayload getWardByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return wardCache.computeIfAbsent(code, this::fetchWard);
    }

    private ProvincePayload fetchProvince(String code) {
        try {
            ResponseEntity<ProvincePayload> response = getRestTemplate()
                    .getForEntity(baseUrl + "/p/" + code, ProvincePayload.class);
            return response.getBody();
        } catch (Exception ex) {
            log.warn("Failed to resolve province name for code {}", code, ex);
            return null;
        }
    }

    private DistrictPayload fetchDistrict(String code) {
        try {
            ResponseEntity<DistrictPayload> response = getRestTemplate()
                    .getForEntity(baseUrl + "/d/" + code, DistrictPayload.class);
            return response.getBody();
        } catch (Exception ex) {
            log.warn("Failed to resolve district name for code {}", code, ex);
            return null;
        }
    }

    private WardPayload fetchWard(String code) {
        try {
            ResponseEntity<WardPayload> response = getRestTemplate()
                    .getForEntity(baseUrl + "/w/" + code, WardPayload.class);
            return response.getBody();
        } catch (Exception ex) {
            log.warn("Failed to resolve ward name for code {}", code, ex);
            return null;
        }
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            synchronized (this) {
                if (restTemplate == null) {
                    restTemplate = restTemplateBuilder
                            .setConnectTimeout(Duration.ofSeconds(3))
                            .setReadTimeout(Duration.ofSeconds(3))
                            .build();
                }
            }
        }
        return restTemplate;
    }

    private boolean isLikelyCode(String value) {
        return StringUtils.hasText(value) && value.trim().chars().allMatch(Character::isDigit);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProvincePayload {
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DistrictPayload {
        private String code;
        private String name;

        @JsonProperty("province_code")
        private String provinceCode;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WardPayload {
        private String code;
        private String name;

        @JsonProperty("district_code")
        private String districtCode;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDistrictCode() {
            return districtCode;
        }

        public void setDistrictCode(String districtCode) {
            this.districtCode = districtCode;
        }
    }
}
