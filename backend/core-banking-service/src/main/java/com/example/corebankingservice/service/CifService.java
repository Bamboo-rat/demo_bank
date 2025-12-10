package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.CreateCifRequest;
import com.example.corebankingservice.dto.request.UpdateCifStatusRequest;
import com.example.corebankingservice.dto.request.UpdateKycStatusRequest;
import com.example.corebankingservice.dto.response.CifResponse;
import com.example.corebankingservice.dto.response.CifStatusResponse;

public interface CifService {

	CifResponse createCif(CreateCifRequest request);

	CifStatusResponse getCifStatus(String cifNumber);

	CifResponse updateCifStatus(String cifNumber, UpdateCifStatusRequest request);

	CifResponse updateKycStatus(String cifNumber, UpdateKycStatusRequest request);
}
