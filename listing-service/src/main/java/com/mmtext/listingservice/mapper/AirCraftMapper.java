package com.mmtext.listingservice.mapper;

import com.mmtext.listingservice.dto.AirCraftAddOrUpdateRequestDTO;
import com.mmtext.listingservice.model.AirCraft;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;

public class AirCraftMapper {
    public static AirCraft toModel(AirCraftAddOrUpdateRequestDTO request) {
        AirCraft airCraft = new AirCraft();
        BeanUtils.copyProperties(request,airCraft);
        return airCraft;
    }
}
