package com.hp.sv.persistence.impl;

import com.hp.sv.model.api.ModelEntity;
import com.hp.sv.persistence.api.ModelEntityRepository;
import org.apache.commons.lang3.Validate;

public class ModelEntityRepositoryImpl implements ModelEntityRepository {

    @Override
    public ModelEntity GetById(int id) {
        Validate.isTrue(id > 0);
        return null;
    }
}
