package com.example.ppis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ppis.dao.ContactMethodDAO;
import com.example.ppis.dao.DepartmentDAO;
import com.example.ppis.dao.RegisteredUserDAO;
import com.example.ppis.dao.RequestDAO;
import com.example.ppis.models.ContactMethod;
import com.example.ppis.models.Request;

@Service
public class RequestService {
	
	RequestDAO requestDao ; 
	RegisteredUserDAO registeredUserDao ; 
	ContactMethodDAO contactMethodDao ; 
	DepartmentDAO departmentDao ; 
	
	@Autowired
	public void setDAOs(RequestDAO requestDao
			, RegisteredUserDAO registeredUserDao
			, ContactMethodDAO contactMethodDao
			, DepartmentDAO departmentDao)	{
		
		this.requestDao = requestDao ; 
		this.registeredUserDao = registeredUserDao ; 
		this.contactMethodDao = contactMethodDao ; 
		this.departmentDao = departmentDao ; 
	}
	
	
	
	private boolean validateNewRequest(String registeredUserUsername
			, String contactMethod
			, String title
			, String description
			, Integer urgency)	{
		
		if(registeredUserUsername.length() == 0 || !this.registeredUserDao.existsByUsername(registeredUserUsername))
			throw new IllegalArgumentException("The user was not specified or does not exist.") ;
		
		if(contactMethod.length() == 0 || !this.contactMethodDao.existsByContactMethodName(contactMethod))
			throw new IllegalArgumentException("The given contact method is either empty or is not supported.") ;
		
		if(title.length() == 0)
			throw new IllegalArgumentException("The title is not specified.") ; 
		
		if(description.length() == 0)
			throw new IllegalArgumentException("The description is not specivied.") ;
		
		if(urgency == null || urgency <= 0)
			throw new IllegalArgumentException("The urgency is either unspecified or is a negative number.") ;
		
		return true ; 
	}
	private boolean validateUnassignedRequest(long requestId
			, String resolverUsername
			, String departmentName
			, Integer priority)	{
		
		if(requestId < 0 || !this.requestDao.existsById(requestId))
			throw new IllegalArgumentException("The request id is not valid or the request does not exist.") ;
		
		if(resolverUsername.length() == 0 || !this.registeredUserDao.existsByUsername(resolverUsername))
			throw new IllegalArgumentException("The resolver username is unspecified or the user does not exist. ") ; 
		
		if(departmentName.length() == 0 ||  !this.departmentDao.existsByDepartmentName(departmentName))
			throw new IllegalArgumentException("The department name is either unspecified or the department does not exist") ; 
		
		if(priority == null || priority < 0)
			throw new IllegalAccessError("The priority is either unspecified or is a negative number.") ;
		
		return true ; 
	}
	private boolean validateReassign(long requestId
			, String resolverUsername
			, String departmentName)	{
		
		if(requestId < 0 || !this.requestDao.existsById(requestId)) 
			throw new IllegalArgumentException("The request id is either a negative number or the request does not exist.") ; 
		
		if(resolverUsername.length() == 0 || !this.registeredUserDao.existsByUsername(resolverUsername))
			throw new IllegalArgumentException("The resolver username is not specified or the user does not exist.") ; 
		
		if(departmentName.length() == 0 || this.departmentDao.existsByDepartmentName(departmentName))	
			throw new IllegalArgumentException("The department name is not specified or the department does not exist.") ; 
		
		return true ; 
	}
	
	public String saveNewRequest(String registeredUserUsername
			, String contactMethod
			, String title
			, String description
			, Integer urgency
			)	{
		
		Request request ; 
		
		try {
			
			if(validateNewRequest(registeredUserUsername
					, contactMethod
					, title
					, description
					, urgency))	{
			
			if(contactMethod.equals("email"))
				request = new Request(this.registeredUserDao.findUserByUsername(registeredUserUsername)
						, this.contactMethodDao.getContactMethodByName(contactMethod)
						, title
						, description
						, urgency
						, this.registeredUserDao.findUserByUsername(registeredUserUsername).getEmail()) ;
			
			else request = new Request(this.registeredUserDao.findUserByUsername(registeredUserUsername)
						, this.contactMethodDao.getContactMethodByName(contactMethod)
						, title
						, description
						, urgency
						, this.registeredUserDao.findUserByUsername(registeredUserUsername).getPhoneNumber()) ;
			
			this.requestDao.create(request) ; 
			
			}
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return "A new request has been created." ;
	}
	public String assignRequest(long unassignedrequestId
			, String resolverUsername
			, String departmentName
			, Integer priority)	{
		
		try {
			if(validateUnassignedRequest(unassignedrequestId
					, resolverUsername
					, departmentName
					, priority))	{
				Request unassignedRequest = this.requestDao.one(unassignedrequestId) ;
				
				unassignedRequest.setResolverUser(this.registeredUserDao.findUserByUsername(resolverUsername)) ;
				unassignedRequest.setDepartment(this.departmentDao.getDepartmentByName(departmentName)) ;
				unassignedRequest.setPriority(priority) ;
				
				this.requestDao.create(unassignedRequest) ; 
			}
		} catch (Exception e) {
			throw e ; 
		}
		
		return "The request has been assigned to the user " + resolverUsername ; 
	}
	public String reassignRequest(long requestId
			, String resolverUsername
			, String departmentName)	{
		
		try {
			
			if(validateReassign(requestId
					, resolverUsername
					, departmentName))	{
				
				Request reassignedRequest = this.requestDao.one(requestId) ; 
				
				reassignedRequest.setResolverUser(this.registeredUserDao.findUserByUsername(resolverUsername)) ;
				reassignedRequest.setDepartment(this.departmentDao.getDepartmentByName(departmentName)) ;
				
				this.requestDao.create(reassignedRequest) ; 
			}
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return "The request has been reassigned to the user " + resolverUsername ; 
	}

	public List<Request> getRequestsByRegisteredUser(String registeredUserUsername)	{
		
		List<Request> requests ; 
		
		try {
			
			if(registeredUserUsername.length() > 0 && this.registeredUserDao.existsByUsername(registeredUserUsername))
				requests = this.requestDao.getRequestsByRegisteredUser(
					this.registeredUserDao.findUserByUsername(registeredUserUsername)) ;
			
			else throw new IllegalArgumentException("The username is either unspecified or the user has no requests.") ; 
		
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestsByResolver(String resolverUsername)	{
		
		List<Request> requests ; 
		
		try {
			if(resolverUsername.length() > 0 && this.registeredUserDao.existsByUsername(resolverUsername))
				requests = this.requestDao.getRequestsByResolver(
						this.registeredUserDao.findUserByUsername(resolverUsername)) ;
			else throw new IllegalArgumentException("The username is either unspecified or the user has no requests.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}

	public List<Request> getRequestsByResolverUserAndGreaterPriority(String resolverUsername, int priority)	{
		
		List<Request> requests ; 
		
		try {
			if(priority > 0 && this.registeredUserDao.existsByUsername(resolverUsername))
				requests = this.requestDao.getRequestssByResolverUserAndGreaterPriority(
						this.registeredUserDao.findUserByUsername(resolverUsername), priority);
			
			else throw new IllegalArgumentException("The priority is less or equal to zero or the resolver is either unspecified or does not exist.");
		} catch (Exception e) {
			throw e ; 
		}	
		
		return requests ; 
	}
	public List<Request> getRequestsByResolverUserAndLowerPriority(String resolverUsername, int priority)	{
		
		List<Request> requests ; 
		
		try {
			if(priority > 0 && this.registeredUserDao.existsByUsername(resolverUsername))
				requests = this.requestDao.getRequestsByResolverUserAndLowerPriority(
						this.registeredUserDao.findUserByUsername(resolverUsername), priority);
			
			else throw new IllegalArgumentException("The priority is less or equal to zero or the resolver is either unspecified or does not exist.");
		} catch (Exception e) {
			throw e ; 
		}	
		
		return requests ; 
	}

	public List<Request> getRequestByRegisteredUserAndGreaterUrgency(String registeredUser, int urgency)	{
		
		List<Request> requests ; 
		
		try {
			if(urgency > 0 && registeredUser.length() > 0 && this.registeredUserDao.existsByUsername(registeredUser))	
				requests = this.requestDao.getRequestsByRegisteredUserAndGreaterUrgency(
						this.registeredUserDao.findUserByUsername(registeredUser), urgency) ;
			else throw new IllegalArgumentException("The urgency is a negative number or the user is unspecified or doesn't exist.") ;
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestByRegisteredUserAndLowerUrgency(String registeredUser, int urgency)	{
		List<Request> requests ; 
		
		try {
			if(urgency > 0 && registeredUser.length() > 0 && this.registeredUserDao.existsByUsername(registeredUser))	
				requests = this.requestDao.getRequestsByRegisteredUserAndLowerUrgency(
						this.registeredUserDao.findUserByUsername(registeredUser), urgency) ;
			else throw new IllegalArgumentException("The urgency is a negative number or the user is unspecified or doesn't exist.") ;
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestsByResolverUserAndGreaterUrgency(String resolverUser, int urgency)	{
		List<Request> requests ; 
		
		try {
			if(urgency > 0 && resolverUser.length() > 0 && this.registeredUserDao.existsByUsername(resolverUser))	
				requests = this.requestDao.getRequestsByResolverUserAndGreaterUrgency(
						this.registeredUserDao.findUserByUsername(resolverUser), urgency) ;
			else throw new IllegalArgumentException("The urgency is a negative number or the user is unspecified or doesn't exist.") ;
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestsByResolverUserAndLowerUrgency(String resolverUser, int urgency)	{
		List<Request> requests ; 
		
		try {
			if(urgency > 0 && resolverUser.length() > 0 && this.registeredUserDao.existsByUsername(resolverUser))	
				requests = this.requestDao.getRequestsByResolverUserAndLowerUrgency(
						this.registeredUserDao.findUserByUsername(resolverUser), urgency) ;
			else throw new IllegalArgumentException("The urgency is a negative number or the user is unspecified or doesn't exist.") ;
			
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 

	}

	/*
	public List<Request> getRequestsByRegisteredUserAndResolved(String registeredUser, boolean resolved)	{
		
		List<Request> requests ; 
		
		try {
			if(registeredUser.length() > 0 && this.registeredUserDao.existsByUsername(registeredUser))	
				requests = this.requestDao.getRequestsByRegisteredUserAndResolved(
						this.registeredUserDao.findUserByUsername(registeredUser), resolved) ;
			else throw new IllegalArgumentException("The user is either unspecified or does not exist.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestsByResolverUserAndResolved(String resolverUser, boolean resolved)	{
		
		List<Request> requests ; 
		
		try {
			if(resolverUser.length() > 0 && this.registeredUserDao.existsByUsername(resolverUser))	
				requests = this.requestDao.getRequestsByResolverUserAndResolved(
						this.registeredUserDao.findUserByUsername(resolverUser), resolved) ;
			else throw new IllegalArgumentException("The user is either unspecified or does not exist.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}*/

	public List<Request> getRequestsByRegisteredUserAndClosed(String registeredUser, boolean closed)	{
		
		List<Request> requests ; 
		
		try {
			if(registeredUser.length() > 0 && this.registeredUserDao.existsByUsername(registeredUser))	
				requests = this.requestDao.getRequestsByRegisteredUserAndClosed(
						this.registeredUserDao.findUserByUsername(registeredUser), closed) ;
			else throw new IllegalArgumentException("The user is either unspecified or does not exist.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	public List<Request> getRequestsByResolverUserAndClosed(String resolverUser, boolean closed)	{
		
		List<Request> requests ; 
		
		try {
			if(resolverUser.length() > 0 && this.registeredUserDao.existsByUsername(resolverUser))	
				requests = this.requestDao.getRequestsByResolverUserAndClosed(
						this.registeredUserDao.findUserByUsername(resolverUser), closed) ;
			else throw new IllegalArgumentException("The user is either unspecified or does not exist.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 

	}

	public List<Request> getRequestsByDepartment(String department)	{
		
		List<Request> requests ;
		
		try {
			if(department.length() > 0 && this.departmentDao.existsByDepartmentName(department))	
				requests = this.requestDao.getRequestsByDepartment(this.departmentDao.getDepartmentByName(department)) ;
			else throw new IllegalArgumentException("The department name is either unspecified or does not exist.") ;
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
	
	public List<Request> getRequestsByResolverUserAndTitle(String resolverUser, String title)	{
		
		List<Request> requests ; 
		
		try {
			if(title.length() > 0 && resolverUser.length() > 0 && this.registeredUserDao.existsByUsername(resolverUser))	
				requests = this.requestDao.getRequestsByResolverUserAndTitle(
						this.registeredUserDao.findUserByUsername(resolverUser), title);
			else throw new IllegalArgumentException("The title unspecified or the resolver username is either unspecified or the user does not exist,") ;
				
		} catch (Exception e) {
			throw e ; 
		}
		
		return requests ; 
	}
}
