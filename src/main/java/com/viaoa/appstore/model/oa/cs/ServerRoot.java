// Copied from OATemplate project by OABuilder 07/01/16 07:41 AM
package com.viaoa.appstore.model.oa.cs;

import java.util.*;

import com.viaoa.annotation.*;
import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.propertypath.*;

/**
 * Root Object that is automatically updated between the Server and Clients. ServerController will do the selects for these objects. Model
 * will share these hubs after the application is started.
 */

@OAClass(useDataSource = false, displayProperty = "Id")
public class ServerRoot extends OAObject {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_Id = "Id";
	public static final String P_Id = "Id";

	/*$$Start: ServerRoot1 $$*/
    // lookups, preselects
    public static final String P_ApplicationTypes = "ApplicationTypes";
    public static final String P_AppRuntimes = "AppRuntimes";
    public static final String P_AppServers = "AppServers";
    public static final String P_AppUsers = "AppUsers";
    public static final String P_Environments = "Environments";
    // autoCreateOne
    public static final String P_CreateOneAppRuntimeHub = "CreateOneAppRuntimeHub";
    public static final String P_CreateOneAppServerHub = "CreateOneAppServerHub";
    // filters
    // UI containers
    public static final String P_AppUserApplicationTypes = "AppUserApplicationTypes";
    public static final String P_ConnectedAppUserLogins = "ConnectedAppUserLogins";
    public static final String P_LastDayAppUserLogins = "LastDayAppUserLogins";
    public static final String P_AppUserErrors = "AppUserErrors";
/*$$End: ServerRoot1 $$*/

	protected int id;
	/*$$Start: ServerRoot2 $$*/
    // lookups, preselects
    protected transient Hub<ApplicationType> hubApplicationTypes;
    protected transient Hub<AppRuntime> hubAppRuntimes;
    protected transient Hub<AppServer> hubAppServers;
    protected transient Hub<AppUser> hubAppUsers;
    protected transient Hub<Environment> hubEnvironments;
    // autoCreateOne
    protected transient Hub<AppRuntime> hubCreateOneAppRuntime;
    protected transient Hub<AppServer> hubCreateOneAppServer;
    // filters
    // UI containers
    protected transient Hub<ApplicationType> hubAppUserApplicationTypes;
    protected transient Hub<AppUserLogin> hubConnectedAppUserLogins;
    protected transient Hub<AppUserLogin> hubLastDayAppUserLogins;
    protected transient Hub<AppUserError> hubAppUserErrors;
/*$$End: ServerRoot2 $$*/

	public ServerRoot() {
		setId(777);
	}

	@OAProperty(displayName = "Id")
	@OAId
	public int getId() {
		return id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTY_Id, old, id);
	}

	/*$$Start: ServerRoot3 $$*/
    // lookups, preselects
    @OAMany(toClass = ApplicationType.class, cascadeSave = true)
    public Hub<ApplicationType> getApplicationTypes() {
        if (hubApplicationTypes == null) {
            hubApplicationTypes = (Hub<ApplicationType>) super.getHub(P_ApplicationTypes);
        }
        return hubApplicationTypes;
    }
    @OAMany(toClass = AppRuntime.class, cascadeSave = true)
    public Hub<AppRuntime> getAppRuntimes() {
        if (hubAppRuntimes == null) {
            hubAppRuntimes = (Hub<AppRuntime>) super.getHub(P_AppRuntimes);
        }
        return hubAppRuntimes;
    }
    @OAMany(toClass = AppServer.class, cascadeSave = true)
    public Hub<AppServer> getAppServers() {
        if (hubAppServers == null) {
            hubAppServers = (Hub<AppServer>) super.getHub(P_AppServers);
        }
        return hubAppServers;
    }
    @OAMany(toClass = AppUser.class, cascadeSave = true)
    public Hub<AppUser> getAppUsers() {
        if (hubAppUsers == null) {
            hubAppUsers = (Hub<AppUser>) super.getHub(P_AppUsers);
        }
        return hubAppUsers;
    }
    @OAMany(toClass = Environment.class, sortProperty = Environment.P_Seq, cascadeSave = true)
    public Hub<Environment> getEnvironments() {
        if (hubEnvironments == null) {
            hubEnvironments = (Hub<Environment>) super.getHub(P_Environments, Environment.P_Seq, true);
        }
        return hubEnvironments;
    }
    // autoCreatedOne
    @OAMany(toClass = AppRuntime.class, cascadeSave = true)
    public Hub<AppRuntime> getCreateOneAppRuntimeHub() {
        if (hubCreateOneAppRuntime == null) {
            hubCreateOneAppRuntime = (Hub<AppRuntime>) super.getHub(P_CreateOneAppRuntimeHub);
        }
        return hubCreateOneAppRuntime;
    }
    @OAMany(toClass = AppServer.class, cascadeSave = true)
    public Hub<AppServer> getCreateOneAppServerHub() {
        if (hubCreateOneAppServer == null) {
            hubCreateOneAppServer = (Hub<AppServer>) super.getHub(P_CreateOneAppServerHub);
        }
        return hubCreateOneAppServer;
    }
    // filters
    // UI containers
    @OAMany(toClass = ApplicationType.class, isCalculated = true, cascadeSave = true)
    public Hub<ApplicationType> getAppUserApplicationTypes() {
        if (hubAppUserApplicationTypes == null) {
            hubAppUserApplicationTypes = (Hub<ApplicationType>) super.getHub(P_AppUserApplicationTypes);
            String pp = AppUserPP.applicationTypes().pp;
            HubMerger hm = new HubMerger(this.getAppUsers(), hubAppUserApplicationTypes, pp, false, true);
        }
        return hubAppUserApplicationTypes;
    }
    @OAMany(toClass = AppUserLogin.class, cascadeSave = true, isProcessed = true)
    public Hub<AppUserLogin> getConnectedAppUserLogins() {
        if (hubConnectedAppUserLogins == null) {
            hubConnectedAppUserLogins = (Hub<AppUserLogin>) super.getHub(P_ConnectedAppUserLogins);
        }
        return hubConnectedAppUserLogins;
    }
    @OAMany(toClass = AppUserLogin.class, isCalculated = true, cascadeSave = true)
    public Hub<AppUserLogin> getLastDayAppUserLogins() {
        if (hubLastDayAppUserLogins == null) {
            hubLastDayAppUserLogins = (Hub<AppUserLogin>) super.getHub(P_LastDayAppUserLogins);
            String pp = AppUserPP.appUserLogins().lastDayFilter().pp;
            HubMerger hm = new HubMerger(this.getAppUsers(), hubLastDayAppUserLogins, pp, false, true);
        }
        return hubLastDayAppUserLogins;
    }
    @OAMany(toClass = AppUserError.class, isCalculated = true, cascadeSave = true)
    public Hub<AppUserError> getAppUserErrors() {
        if (hubAppUserErrors == null) {
            hubAppUserErrors = (Hub<AppUserError>) super.getHub(P_AppUserErrors);
            String pp = AppUserPP.appUserLogins().appUserErrors().pp;
            HubMerger hm = new HubMerger(this.getAppUsers(), hubAppUserErrors, pp, false, true);
        }
        return hubAppUserErrors;
    }
/*$$End: ServerRoot3 $$*/
}
