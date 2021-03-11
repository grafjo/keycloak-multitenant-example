package com.example.demo;

import org.junit.jupiter.api.Test;

import static com.example.demo.TenantPathResolver.extractTenantId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantPathResolverTest {

    @Test
    void path() {
        assertThat(extractTenantId("http://example.org/tenant/e1c28952/oders/5/details")).isEqualTo("e1c28952");
    }

    @Test
    void query() {
        assertThat(extractTenantId("http://example.org/tenant/e1c28952?sort=desc")).isEqualTo("e1c28952");
    }

    @Test
    void detectsUrlWithoutTenantPattern() {
        assertThatThrownBy(() ->
                extractTenantId("http://example.org/orders/5/details")
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("no tenant-pattern found in the request path!");
    }
}
