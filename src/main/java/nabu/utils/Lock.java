package nabu.utils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ClusteredServer;
import be.nabu.libs.cluster.api.ClusterInstance;
import be.nabu.libs.cluster.api.ClusterLock;
import be.nabu.libs.cluster.local.LocalInstance;
import be.nabu.libs.services.TransactionCloseable;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.ServiceException;

@WebService
public class Lock {
	
	private ExecutionContext context;
	
	// no longer destroying locks, we generally don't use variable lock names so it shouldn't pile up too much in memory (otherwise this may become a memory leak!)
	// the problem is if we unlock it and then destroy it (without any kind of synchronization), we can interfere with other cluster members who just got the lock
	// hazelcast documentation is not clear in how they resolve this, in their examples there is no destroy
	// creating a new lock to destroy this lock seems overkill
	public void lock(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local) {
		final ClusterLock lock = getCluster(local).lock(name);
		lock.lock();

		// make sure we release the lock in case the runtime ends suddenly (e.g. abort)
		context.getTransactionContext().add(null, new TransactionCloseable(new AutoCloseable() {
			@Override
			public void close() throws Exception {
				if (lock.isLockedByCurrentThread()) {
					lock.unlock();
				}
//				lock.destroy();
			}
		}));
	}
	@WebResult(name = "locked")
	public boolean tryLock(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local) {
		final ClusterLock lock = getCluster(local).lock(name);
		if (lock.tryLock()) {
			context.getTransactionContext().add(null, new TransactionCloseable(new AutoCloseable() {
				@Override
				public void close() throws Exception {
					if (lock.isLockedByCurrentThread()) {
						lock.unlock();
					}
//					lock.destroy();
				}
			}));
			return true;
		}
		return false;
	}
	@WebResult(name = "locked")
	public boolean isLocked(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local) {
		return getCluster(local).lock(name).isLocked();
	}
	public void unlock(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local) throws ServiceException {
		ClusterLock lock = getCluster(local).lock(name);
		try {
			lock.unlock();
		}
		catch (Exception e) {
			throw new ServiceException("LOCK-1", "Could not unlock: " + name, e);
		}
	}
	private ClusterInstance getCluster(Boolean local) {
		if (local == null || !local) {
			if (EAIResourceRepository.getInstance().getServiceRunner() instanceof ClusteredServer) {
				return ((ClusteredServer) EAIResourceRepository.getInstance().getServiceRunner()).getCluster();
			}
			else {
				throw new IllegalStateException("The server does not support clustered locking");
			}
		}
		else {
			return LocalInstance.getInstance();
		}
	}
}
