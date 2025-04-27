package com.maja.complaints.service;

import org.springframework.stereotype.Service;

@Service
public interface LocationService {

    String locateOrDefault(String ipAddressString, String fallbackCountryVale);
}
