package com.ttukttak.week.presentation;

import com.ttukttak.week.application.WeekFacade;
import com.ttukttak.week.presentation.request.ImportWeeksRequest;
import com.ttukttak.week.presentation.request.UpdateWeekRequest;
import com.ttukttak.week.presentation.response.WeekDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/weeks")
@RequiredArgsConstructor
public class AdminWeekControllerImpl implements AdminWeekController {
    private final WeekFacade weekFacade;

    @Override
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void importWeeks(@Valid @RequestBody ImportWeeksRequest request) {
        weekFacade.importWeeks(request);
    }

    @Override
    @PatchMapping("/{weekId}")
    public WeekDetailResponse update(@PathVariable String weekId, @Valid @RequestBody UpdateWeekRequest request) {
        return weekFacade.update(weekId, request);
    }

    @Override
    @PostMapping("/{weekId}/announce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void announce(@PathVariable String weekId) {
        weekFacade.announce(weekId);
    }
}
