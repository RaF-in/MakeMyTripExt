package com.mmtext.listingservice.mapper;

import com.mmtext.listingservice.dto.ConcertRequestDTO;
import com.mmtext.listingservice.model.Address;
import com.mmtext.listingservice.model.Concert;
import org.springframework.beans.BeanUtils;

public class ConcertMapper {
    public static Concert toModel(ConcertRequestDTO request) {
        Concert concert = new Concert();
        BeanUtils.copyProperties(request,concert);
        Address venue = new Address();
        BeanUtils.copyProperties(request.getAddressRequestDTO(), venue);
        concert.setVenue(venue);
        return concert;
    }
}
