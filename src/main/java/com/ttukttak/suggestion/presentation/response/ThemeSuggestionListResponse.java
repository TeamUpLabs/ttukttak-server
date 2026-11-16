package com.ttukttak.suggestion.presentation.response;

import java.util.List;

public record ThemeSuggestionListResponse(List<ThemeSuggestionResponse> items, String nextCursor) {
}
