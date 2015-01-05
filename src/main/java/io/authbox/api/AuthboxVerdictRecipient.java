package io.authbox.api;

import io.authbox.api.AuthboxVerdict;

interface AuthboxVerdictRecipient {
    public void receiveVerdict(AuthboxVerdict verdict);
}
