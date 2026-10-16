# Hubitat용 Awair Omni 드라이버

[English](README.en.md)

## 소개
[Awair Omni](https://www.getawair.com/products/omni) 공기질 측정기용 Hubitat 드라이버입니다.
기기에 내장된 로컬 API를 사용하므로 클라우드 없이도 안정적이고 설정이 간단합니다.
조도·소음 센서가 없는 Awair R2도 사용할 수 있으며, R2에서는 해당 값만 건너뜁니다.

## 기능
Awair Omni가 제공하는 다음 측정값을 지원합니다.

- AIQ 점수
- 온도 (허브에 설정된 단위)
- 상대 습도 (%)
- 이산화탄소(CO2) 농도 (ppm)
- 총휘발성유기화합물(TVOC) 농도 (ppb)
- PM2.5 농도 (ug/m3)
- 조도 (lux)
- 소음 (dB)

## 설치
- **HPM:** Hubitat Package Manager에서 `Awair Omni Local` 패키지를 검색해 설치합니다.
- **수동:** Hubitat 관리 화면 → **Drivers Code** → **New Driver** → 아래 주소로 Import 하거나 [`Awair_Onmi_Local.groovy`](Awair_Onmi_Local.groovy) 내용을 붙여넣고 Save 합니다.
  `https://raw.githubusercontent.com/kwon2288/Hubitat-AwAir/master/Awair_Onmi_Local.groovy`

## 사용 방법
1. **Devices → Add Device → Virtual**에서 Type을 `Awair Omni Local`로 선택해 기기를 만듭니다.
2. Preferences에 Awair의 로컬 IP를 입력하고 **Save Preferences**를 누릅니다. 저장하면 바로 폴링을 시작합니다.
   - IP 앞뒤 공백, `http://`, 끝의 `/`는 자동으로 정리됩니다.
   - API Path 기본값은 `/air-data/latest`, 폴링 간격 기본값은 300초입니다.

## 문제 해결
- 요청이 실패하면 로그에 `Awair request to http://<IP> failed: HTTP <상태 코드> ...`가 남습니다.
- IP를 바꾼 뒤 값이 갱신되지 않으면 Preferences를 저장하고, 허브와 같은 네트워크에서 `http://<IP>/air-data/latest`가 열리는지 확인하세요.
- 자세한 로그가 필요하면 `Enable debug logging`을 켜세요.
