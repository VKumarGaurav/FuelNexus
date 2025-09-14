package com.fuel.nexus.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}

//CustomerFeedback feedback = modelMapper.map(feedbackDTO, CustomerFeedback.class);
//CustomerFeedbackDTO dto = modelMapper.map(feedback, CustomerFeedbackDTO.class);
