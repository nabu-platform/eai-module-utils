/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
	// @2022-09-02: discovered a potential memory leak for long running (daemon) services
	// the root transaction is never committed for daemons, so the transactioncloseable we add to that root transaction may never be committed unless you explicitly manage it via sequence or do an intentional commit
	// e.g. the task executor daemon performed a tryLock and a correctly scoped unlock, but failed to account for the buildup in the root transaction
	// one task-heavy server in particular that was looked at had (over a period of time) accumulated 1.1GB of objects from the tryLock, out of a heapdump of 2.5gb
	// we advise sequence based management of default transactions because this is easiest, especially in daemons
	// but in those rare cases that it is relevant, we want to give you control over the transaction it ends up in which is why the transactionId was added as a way of controlling this
	public void lock(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local, @WebParam(name = "transactionId") java.lang.String transactionId) {
		final ClusterLock lock = getCluster(local).lock(name);
		lock.lock();

		// make sure we release the lock in case the runtime ends suddenly (e.g. abort)
		context.getTransactionContext().add(transactionId, new TransactionCloseable(new AutoCloseable() {
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
	public boolean tryLock(@WebParam(name = "name") java.lang.String name, @WebParam(name = "local") Boolean local, @WebParam(name = "transactionId") java.lang.String transactionId) {
		final ClusterLock lock = getCluster(local).lock(name);
		if (lock.tryLock()) {
			context.getTransactionContext().add(transactionId, new TransactionCloseable(new AutoCloseable() {
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
