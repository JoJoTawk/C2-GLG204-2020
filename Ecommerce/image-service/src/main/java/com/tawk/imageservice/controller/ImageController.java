package com.tawk.imageservice.controller;

import com.tawk.imageservice.entity.ImageResponse;
import com.tawk.imageservice.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/images")
    public ImageResponse getAllImages(){
        return new ImageResponse(imageRepository.findAll());
    }
}
