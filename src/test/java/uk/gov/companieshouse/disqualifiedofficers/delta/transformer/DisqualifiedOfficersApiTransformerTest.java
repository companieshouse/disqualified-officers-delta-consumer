package uk.gov.companieshouse.disqualifiedofficers.delta.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.InternalCorporateDisqualificationMapper;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.InternalNaturalDisqualificationMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficersApiTransformerTest {

    @Mock
    private InternalNaturalDisqualificationMapper naturalMapper;
    @Mock
    private InternalCorporateDisqualificationMapper corporateMapper;
    private DisqualifiedOfficersApiTransformer transformer;

    @BeforeEach
    public void setup() {
         transformer = new DisqualifiedOfficersApiTransformer(
            corporateMapper, naturalMapper
        );
    }

    @Test
    public void transformNaturalDeltaSuccessfully() {
        DisqualificationDelta input = new DisqualificationDelta();
        List<DisqualificationOfficer> disqualifiedOfficerList = new ArrayList<>();
        disqualifiedOfficerList.add(new DisqualificationOfficer());
        input.setDisqualifiedOfficer(disqualifiedOfficerList);
        input.setDeltaAt("20211008152823383176");

        DisqualificationOfficer disqualificationOfficer = input.getDisqualifiedOfficer().get(0);
        InternalNaturalDisqualificationApi mock = mock(InternalNaturalDisqualificationApi.class);

        when(naturalMapper.disqualificationDeltaToApi(disqualificationOfficer)).thenReturn(mock);

        InternalNaturalDisqualificationApi actual = transformer.transformNaturalDisqualification(input);
        assertThat(actual).isEqualTo(mock);
    }

    @Test
    public void transformCorporateDeltaSuccessfully() {
        DisqualificationDelta input = new DisqualificationDelta();
        List<DisqualificationOfficer> disqualifiedOfficerList = new ArrayList<>();
        disqualifiedOfficerList.add(new DisqualificationOfficer());
        input.setDisqualifiedOfficer(disqualifiedOfficerList);
        input.setDeltaAt("20211008152823383176");

        DisqualificationOfficer disqualificationOfficer = input.getDisqualifiedOfficer().get(0);
        InternalCorporateDisqualificationApi mock = mock(InternalCorporateDisqualificationApi.class);

        when(corporateMapper.disqualificationDeltaToApi(disqualificationOfficer)).thenReturn(mock);

        InternalCorporateDisqualificationApi actual = transformer.transformCorporateDisqualification(input);
        assertThat(actual).isEqualTo(mock);
    }

    @Test
    void errorDuringTransformationThrowsNullPointer() {
        DisqualificationDelta input = new DisqualificationDelta();
        List<DisqualificationOfficer> disqualifiedOfficerList = new ArrayList<>();
        disqualifiedOfficerList.add(new DisqualificationOfficer());
        input.setDisqualifiedOfficer(disqualifiedOfficerList);

        DisqualificationOfficer disqualificationOfficer = input.getDisqualifiedOfficer().get(0);

        when(naturalMapper.disqualificationDeltaToApi(disqualificationOfficer))
                .thenThrow(NullPointerException.class);
        assertThrows(NullPointerException.class, () -> transformer.transformNaturalDisqualification(input));
    }
}