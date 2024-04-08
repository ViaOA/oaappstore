// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.testserver;

import com.viaoa.appstore.datasource.DataSource;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.datasource.objectcache.OADataSourceObjectCache;

/**
 * barebones test server, with only db connection.
 * @author vvia
 *
 */
public class TestServerController {

    public TestServerController() {
    }
    
    public void start() throws Exception {
        Resource.setRunType(Resource.RUNTYPE_Server);
        Resource.getServerProperties();

        String driver = Resource.getValue(Resource.DB_JDBC_Driver);

        DataSource dataSource = new DataSource();
        dataSource.open();
        dataSource.getOADataSource().setAssignIdOnCreate(true);

        OADataSourceObjectCache dsObjectCache = new OADataSourceObjectCache(); // for non-DB objects
    }
    
    
    public static void main(String[] args) throws Exception {
        TestServerController cont = new TestServerController();
        System.out.println("calling start");
        cont.start();
        System.out.println("done");
    }
    
}
