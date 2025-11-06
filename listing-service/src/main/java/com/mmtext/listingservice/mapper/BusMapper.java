package com.mmtext.listingservice.mapper;

import com.mmtext.listingservice.dto.BusAddOrUpdateRequestDTO;
import com.mmtext.listingservice.model.Bus;
import org.springframework.beans.BeanUtils;

public class BusMapper {
    public static Bus toModel(BusAddOrUpdateRequestDTO request) {
        Bus bus = new Bus();
        BeanUtils.copyProperties(request,bus);
        return bus;
    }
}
