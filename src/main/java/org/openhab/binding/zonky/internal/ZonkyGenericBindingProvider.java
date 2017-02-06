/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zonky.internal;

import org.openhab.binding.zonky.ZonkyBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;


/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author Ondrej Pecta
 * @since 1.9.0
 */
public class ZonkyGenericBindingProvider extends AbstractGenericBindingProvider implements ZonkyBindingProvider {

    /**
     * {@inheritDoc}
     */
    public String getBindingType() {
        return "zonky";
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
        //if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
        //	throw new BindingConfigParseException("item '" + item.getName()
        //			+ "' is of type '" + item.getClass().getSimpleName()
        //			+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
        //}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
        super.processBindingConfiguration(context, item, bindingConfig);
        ZonkyBindingConfig config = new ZonkyBindingConfig(bindingConfig);
        addBindingConfig(item, config);
    }

    @Override
    public String getItemType(String itemName) {
        final ZonkyBindingConfig config = (ZonkyBindingConfig) this.bindingConfigs.get(itemName);
        return config != null ? (config.getType()) : null;
    }


    /**
     * This is a helper class holding binding specific configuration details
     *
     * @author Ondrej Pecta
     * @since 1.9.0
     */
    class ZonkyBindingConfig implements BindingConfig {
        // put member fields here which holds the parsed values
        private String type;

        public ZonkyBindingConfig(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }


}
