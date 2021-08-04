package io.metrix.mnemosyne.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthService {
    @GetMapping("/health")
    fun health(): String {
        return "ok"
    }
}
