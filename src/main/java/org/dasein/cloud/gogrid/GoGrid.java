/**
 * ========= CONFIDENTIAL =========
 *
 * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
 *
 * ====================================================================
 *  NOTICE: All information contained herein is, and remains the
 *  property of enStratus Networks Inc. The intellectual and technical
 *  concepts contained herein are proprietary to enStratus Networks Inc
 *  and may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law. Dissemination
 *  of this information or reproduction of this material is strictly
 *  forbidden unless prior written permission is obtained from
 *  enStratus Networks Inc.
 * ====================================================================
 */
package org.dasein.cloud.gogrid;

import org.apache.log4j.Logger;
import org.dasein.cloud.AbstractCloud;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.gogrid.compute.GoGridCompute;
import org.dasein.cloud.gogrid.network.GoGridNetworking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base cloud provider implementation for bootstrapping GoGrid API interaction.
 * <p>Created by George Reese: 10/13/12 1:38 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class GoGrid extends AbstractCloud {
    static private final Logger logger = getLogger(GoGrid.class);

    static private String getLastItem(String name) {
        int idx = name.lastIndexOf('.');

        if( idx < 0 ) {
            return name;
        }
        else if( idx == (name.length()-1) ) {
            return "";
        }
        return name.substring(idx+1);
    }

    static public Logger getLogger(Class<?> cls) {
        String pkg = getLastItem(cls.getPackage().getName());

        if( pkg.equals("gogrid") ) {
            pkg = "";
        }
        else {
            pkg = pkg + ".";
        }
        return Logger.getLogger("dasein.cloud.gogrid.std." + pkg + getLastItem(cls.getName()));
    }

    static public Logger getWireLogger(Class<?> cls) {
        return Logger.getLogger("dasein.cloud.gogrid.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName()));
    }

    public GoGrid() { }

    @Override
    public @Nonnull String getCloudName() {
        ProviderContext ctx = getContext();
        String name = (ctx == null ? null : ctx.getCloudName());

        return (name == null ? "GoGrid" : name);
    }

    @Override
    public @Nonnull GoGridCompute getComputeServices() {
        return new GoGridCompute(this);
    }

    @Override
    public @Nonnull GoGridDC getDataCenterServices() {
        return new GoGridDC(this);
    }

    @Override
    public @Nonnull GoGridNetworking getNetworkServices() {
        return new GoGridNetworking(this);
    }

    @Override
    public @Nonnull String getProviderName() {
        ProviderContext ctx = getContext();
        String name = (ctx == null ? null : ctx.getProviderName());

        return (name == null ? "GoGrid" : name);
    }

    @Override
    public @Nullable String testContext() {
        try {
            GoGridMethod method = new GoGridMethod(this);

            if( method.get(GoGridMethod.LOOKUP_LIST, new GoGridMethod.Param("lookup", "datacenter")) == null ) {
                return null;
            }
            ProviderContext ctx = getContext();

            return (ctx == null ? null : ctx.getAccountNumber());
        }
        catch( CloudException e ) {
            if( e.getHttpCode() == 403 ) {
                return null;
            }
            logger.warn("Failed to test context: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        catch( Throwable t ) {
            logger.warn("Failed to test context: " + t.getMessage());
            t.printStackTrace();
            return null;
        }
    }

    public @Nonnull Architecture toArchitecture(@Nonnull String name) {
        if( name.contains("_32_") ) {
            return Architecture.I32;
        }
        else if( name.contains("_64_") ) {
            return Architecture.I64;
        }
        else if( name.contains("64") ) {
            return Architecture.I64;
        }
        else if( name.contains("32") ) {
            return Architecture.I32;
        }
        return Architecture.I64;
    }
}