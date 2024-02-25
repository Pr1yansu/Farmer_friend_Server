package com.priyansu.authentication.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dr0wxzkgc",
                "api_key", "821555866452945",
                "api_secret", "9nmqcWO38w1BO7NHH5RyVbJ2Ueg"
        ));
    }

    public Map uploadAvatar(byte[] image) {
        try {
            return cloudinary().uploader().upload(image, ObjectUtils.emptyMap());
        } catch (IOException e) {
            e.fillInStackTrace();
            return null;
        }
    }
}
