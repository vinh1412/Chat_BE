/*
 * @ {#} AppConfig.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.entities.Conversation;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Bỏ qua lastMessage khi ánh xạ từ ConversationDTO sang Conversation
        modelMapper.typeMap(ConversationDTO.class, Conversation.class)
                .addMappings(mapper -> {
                    mapper.map(ConversationDTO::getLastMessageId, Conversation::setLastMessageId);
                    mapper.skip(ConversationDTO::getLastMessage, Conversation::setLastMessageId);
                });

        return modelMapper;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
