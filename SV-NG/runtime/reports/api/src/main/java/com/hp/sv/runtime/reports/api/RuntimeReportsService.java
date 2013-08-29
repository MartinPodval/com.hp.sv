package com.hp.sv.runtime.reports.api;

public interface RuntimeReportsService {
    void registerService(int id);
    void increaseServiceUsageCount(int id);
    int getServiceUsageCount(int id);
    void unregisterService(int id);
}
