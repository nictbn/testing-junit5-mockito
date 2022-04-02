package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.fauxspring.BindingResult;
import guru.springframework.sfgpetclinic.fauxspring.Model;
import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final String OWNERS_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String REDIRECT_OWNERS_5 = "redirect:/owners/5";

    @Mock
    OwnerService ownerService;

    @InjectMocks
    OwnerController controller;

    @Mock
    BindingResult bindingResult;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {
        given(ownerService.findAllByLastNameLike(stringArgumentCaptor.capture()))
                .willAnswer(invocation -> {
                    List<Owner> owners = new ArrayList<>();
                    String name = invocation.getArgument(0);
                    switch (name) {
                        case "%Buck%":
                            owners.add(new Owner(1L, "Jim", "Bob"));
                            return owners;
                        case "%DontFindMe%":
                            return owners;
                        case "%FindMe%":
                            owners.add(new Owner(1L, "Jim", "Bob"));
                            owners.add(new Owner(1L, "Jim", "Bob"));
                            return owners;
                    }
                    throw new Exception("Invalid Argument");
                });
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void processCreationFormHasErrors() {
        // given
        Owner owner = new Owner(1L, "Jim", "Bob");
        given(bindingResult.hasErrors()).willReturn(true);

        // when
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(OWNERS_CREATE_OR_UPDATE_OWNER_FORM);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void processCreationFormNoErrors() {
        // given
        Owner owner = new Owner(5L, "Jim", "Bob");
        given(bindingResult.hasErrors()).willReturn(false);
        given(ownerService.save(any())).willReturn(owner);

        // when
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(REDIRECT_OWNERS_5);
    }

    @Test
    void processFormWildcardStringWithAnnotation() {
        // given
        Owner owner = new Owner(5L, "Joe", "Buck");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%Buck%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("redirect:/owners/1").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processFormWildcardNotFound() {
        // given
        Owner owner = new Owner(5L, "Joe", "DontFindMe");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%DontFindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("owners/findOwners").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processFormWildcardFound() {
        // given
        Owner owner = new Owner(5L, "Joe", "FindMe");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, Mockito.mock(Model.class));

        // then
        assertThat("%FindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("owners/ownersList").isEqualToIgnoringCase(viewName);
    }
}