package de.ait.homerent.dto;

import org.junit.jupiter.api.Test;

class PropertyMapperTest {

    @Test
    void shouldMapEntityToDto() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner1");

        Property entity = new Property();
        entity.setId(10L);
        entity.setTitle("Nice flat");
        entity.setAddress("Berlin");
        entity.setDescription("Center");
        entity.setPricePerDay(BigDecimal.valueOf(80));
        entity.setOwner(owner);

        PropertyDto dto = PropertyMapper.toDto(entity);

        assertEquals(10L, dto.getId());
        assertEquals("Nice flat", dto.getTitle());
        assertEquals("Berlin", dto.getAddress());
        assertEquals(BigDecimal.valueOf(80), dto.getPricePerDay());
        assertEquals(1L, dto.getOwnerId());
    }
}

