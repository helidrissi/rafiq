package ma.rafiq.application;

import ma.rafiq.domain.LlmResult;

public interface LlmClient { LlmResult classify(String message); }
