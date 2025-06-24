package com.example.erpnextintegration.dto.alea;

import com.example.erpnextintegration.dto.payroll.SalaireDTO;

import java.util.List;

public class SalarySlipResponse {
    private List<SalaireDTO> data;

    public List<SalaireDTO> getData() {
        return data;
    }

    public void setData(List<SalaireDTO> data) {
        this.data = data;
    }
}
