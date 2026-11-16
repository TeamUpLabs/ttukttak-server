package com.ttukttak.subscription.presentation;

import com.ttukttak.subscription.application.SubscriptionFacade;
import com.ttukttak.subscription.presentation.request.SubscribeRequest;
import com.ttukttak.subscription.presentation.response.SubscribeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionControllerImpl implements SubscriptionController {
    private final SubscriptionFacade subscriptionFacade;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubscribeResponse subscribe(@Valid @RequestBody SubscribeRequest request) {
        return subscriptionFacade.subscribe(request);
    }

    @Override
    @GetMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestParam(required = false) String token) {
        return redirect("/?subscribed=" + (subscriptionFacade.confirm(token) ? 1 : 0));
    }

    @Override
    @GetMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam(required = false) String token) {
        return redirect("/?unsubscribed=" + (subscriptionFacade.unsubscribe(token) ? 1 : 0));
    }

    private static ResponseEntity<Void> redirect(String path) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(path)).build();
    }
}
