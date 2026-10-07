package com.crypto.keymaker.controller;

import com.crypto.keymaker.dto.*;
import com.crypto.keymaker.dto.base.KeymakerResponse;
import com.crypto.keymaker.scheduler.OperationalGovernanceSchedulers;
import com.crypto.keymaker.service.BindingService;
import com.crypto.keymaker.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/keymaker")
public class KeyspecOnboardController {
    private final OnboardingService onboardingService;
    private final BindingService bindingService;
    private final OperationalGovernanceSchedulers governance;

    public KeyspecOnboardController(OnboardingService onboardingService, BindingService bindingService, OperationalGovernanceSchedulers governance) {
        this.onboardingService = onboardingService;
        this.bindingService = bindingService;
        this.governance = governance;
    }

    @PostMapping("/onboard")
    public ResponseEntity<KeymakerResponse> onboardKeyspec(
            @RequestBody OnboardRequest request,
            @RequestHeader(value = "correlation_id", required = false) String correlationId) throws Exception {
        String corrId = (correlationId != null) ? correlationId : UUID.randomUUID().toString();
        if (governance.isPipelineThrottled()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(KeymakerResponse.failure(corrId, "12-Hour registration window limit reached."));
        }
        String id = onboardingService.executeOnboard(request, corrId);
        return ResponseEntity.ok(KeymakerResponse.success(corrId, "Keyspec onboarded successfully.", id));
    }

    @PostMapping("/bind")
    public ResponseEntity<KeymakerResponse> bindIdentity(@RequestBody BindRequest request, @RequestHeader(value="correlation_id", required=false) String correlationId) {
        String corrId = (correlationId != null) ? correlationId : UUID.randomUUID().toString();
        bindingService.executeIdentityMutation(request, true, corrId);
        return ResponseEntity.ok(KeymakerResponse.success(corrId, "Identity linkage mapping established.", null));
    }

    @PostMapping("/unbind")
    public ResponseEntity<KeymakerResponse> unbindIdentity(@RequestBody BindRequest request, @RequestHeader(value="correlation_id", required=false) String correlationId) {
        String corrId = (correlationId != null) ? correlationId : UUID.randomUUID().toString();
        bindingService.executeIdentityMutation(request, false, corrId);
        return ResponseEntity.ok(KeymakerResponse.success(corrId, "Identity association detached cleanly.", null));
    }
}
