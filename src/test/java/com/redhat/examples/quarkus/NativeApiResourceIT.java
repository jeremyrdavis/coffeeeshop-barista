package com.redhat.examples.quarkus;

import com.redhat.examples.quarkus.coffeeshop.barista.infrastructure.ApiResourceTest;
import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeApiResourceIT extends ApiResourceTest {

    // Execute the same tests but in native mode.
}