package com.hp.sv.runtime.reports.api;

public interface RuntimeReportsService {

    void RegisterService(int id);
    int GetServiceUsageCount(int id);
    void UnregisterService(int id);
}
