package org.valkyrienskies.buggy.PAL.PinTypes;

import org.valkyrienskies.buggy.PAL.Pin;

public class EmmitingPin extends Pin {
    public EmmitingPin(Long id) {
        super(id);
    }

    @Override
    public void updateValue() {
        this.setValue(1.0);
    }
}
