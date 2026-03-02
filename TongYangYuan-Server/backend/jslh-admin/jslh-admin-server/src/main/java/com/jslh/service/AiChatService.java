package com.jslh.service;

import com.jslh.dto.AiChatMessageDTO;
import com.jslh.dto.AiChatRequestDTO;
import reactor.core.publisher.Flux;

public interface AiChatService {

    Flux<AiChatMessageDTO> sendChatMessage(AiChatRequestDTO requestDTO);

}
