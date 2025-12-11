# Partner Bank Test Accounts

## ACB - Ngân hàng TMCP Á Châu
- Account: 1234567890
- Name: NGUYEN VAN A
- Status: Active

## VCB - Ngân hàng TMCP Ngoại Thương Việt Nam
- Account: 0987654321
- Name: TRAN THI B
- Status: Active

## TCB - Ngân hàng TMCP Kỹ Thương Việt Nam
- Account: 1122334455
- Name: LE VAN C
- Status: Active

## Test Cases

### Valid Account
```bash
curl -X POST http://localhost:8088/api/partner-banks/acb/verify-account?accountNumber=1234567890
```

### Invalid Account
```bash
curl -X POST http://localhost:8088/api/partner-banks/acb/verify-account?accountNumber=0000000000
```

### Get Account Name
```bash
curl http://localhost:8088/api/partner-banks/vcb/account-name?accountNumber=0987654321
```
