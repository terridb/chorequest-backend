package com.terridb.chorequest.utils;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

public final class UrlUtil {
    private UrlUtil() {}

    public static URI buildCreatedUri(String path, UUID id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path)
                .buildAndExpand(id)
                .toUri();
    }
}
