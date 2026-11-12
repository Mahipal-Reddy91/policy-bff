package com.abc.apac.policybff.api.converter;

import com.abc.apac.policybff.domain.entity.LineOfBusiness;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineOfBusinessConverterTest {
	private final LineOfBusinessConverter converter = new LineOfBusinessConverter();

	@Test
	void convertsContractValuesThroughDomainMapping() {
		assertThat(converter.convert("Property")).isEqualTo(LineOfBusiness.Property);
		assertThat(converter.convert("Casualty")).isEqualTo(LineOfBusiness.Casualty);
		assertThat(converter.convert("A&H")).isEqualTo(LineOfBusiness.A_AND_H);
		assertThat(converter.convert("Marine")).isEqualTo(LineOfBusiness.Marine);
	}

	@Test
	void rejectsUnknownValue() {
		assertThatThrownBy(() -> converter.convert("Other"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
