package com.maja.complaints.service;

import org.springframework.stereotype.Service;

@Service
public interface LocationService {

    default String locateOrDefault(String ipAddressString, String fallbackCountryVale) { return fallbackCountryVale; }
}
