/*
 * COPYRIGHT 2019 SEAGATE LLC
 *
 * THIS DRAWING/DOCUMENT, ITS SPECIFICATIONS, AND THE DATA CONTAINED
 * HEREIN, ARE THE EXCLUSIVE PROPERTY OF SEAGATE TECHNOLOGY
 * LIMITED, ISSUED IN STRICT CONFIDENCE AND SHALL NOT, WITHOUT
 * THE PRIOR WRITTEN PERMISSION OF SEAGATE TECHNOLOGY LIMITED,
 * BE REPRODUCED, COPIED, OR DISCLOSED TO A THIRD PARTY, OR
 * USED FOR ANY PURPOSE WHATSOEVER, OR STORED IN A RETRIEVAL SYSTEM
 * EXCEPT AS ALLOWED BY THE TERMS OF SEAGATE LICENSES AND AGREEMENTS.
 *
 * YOU SHOULD HAVE RECEIVED A COPY OF SEAGATE'S LICENSE ALONG WITH
 * THIS RELEASE. IF NOT PLEASE CONTACT A SEAGATE REPRESENTATIVE
 * http://www.seagate.com/contact
 *
 * Original author: Abhilekh Mustapure <abhilekh.mustapure@seagate.com>
 * Original creation date: 22-May-2019
 */

package com.seagates3.controller;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.seagates3.dao.DAODispatcher;
import com.seagates3.dao.DAOResource;
import com.seagates3.dao.UserDAO;
import com.seagates3.dao.UserLoginProfileDAO;
import com.seagates3.exception.DataAccessException;
import com.seagates3.model.Account;
import com.seagates3.model.Requestor;
import com.seagates3.model.User;
import com.seagates3.response.ServerResponse;
import com.seagates3.util.KeyGenUtil;

import io.netty.handler.codec.http.HttpResponseStatus;

@PowerMockIgnore({"javax.management.*"}) @RunWith(PowerMockRunner.class)
    @PrepareForTest(
        {DAODispatcher.class,
         KeyGenUtil.class}) public class UserLoginProfileControllerTest {

 private
  UserLoginProfileController userLoginProfileController;
 private
  UserDAO userDAO;
 private
  UserLoginProfileDAO userLoginProfileDAO;
 private
  final String ACCOUNT_NAME = "s3test";
 private
  final String ACCOUNT_ID = "12345";
 private
  final Account ACCOUNT;
 private
  final String USERID = "123";
 private
  final String USERNAME = "s3testuser";

 private
  UserDAO mockUserDao;
 private
  Map<String, String> requestBodyObj = null;
 private
  Requestor requestorObj = null;
 private
  final String GET_RESOURCE_DAO = "getResourceDAO";

 public
  UserLoginProfileControllerTest() {
    ACCOUNT = new Account();
    ACCOUNT.setId(ACCOUNT_ID);
    ACCOUNT.setName(ACCOUNT_NAME);
  }

 private
  void createUserLoginProfileController_CreateAPI() throws Exception {
    Requestor requestor = new Requestor();
    requestor.setAccount(ACCOUNT);

    Map<String, String> requestBody =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    requestBody.put("UserName", "s3testuser");
    requestBody.put("Password", "abc");

    userDAO = Mockito.mock(UserDAO.class);
    userLoginProfileDAO = Mockito.mock(UserLoginProfileDAO.class);

    PowerMockito.doReturn(userDAO)
        .when(DAODispatcher.class, "getResourceDAO", DAOResource.USER);

    PowerMockito.doReturn(userLoginProfileDAO).when(
        DAODispatcher.class, "getResourceDAO", DAOResource.USER_LOGIN_PROFILE);

    userLoginProfileController =
        new UserLoginProfileController(requestor, requestBody);
  }

  @Before public void setUp() throws Exception {
    PowerMockito.mockStatic(DAODispatcher.class);
    mockUserDao = Mockito.mock(UserDAO.class);
    requestorObj = new Requestor();
    requestorObj.setAccount(ACCOUNT);
    requestBodyObj = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    requestBodyObj.put("UserName", "s3testuser");
  }

  @Test public void CreateUser_UserSearchFailed_ReturnInternalServerError()
      throws Exception {

    createUserLoginProfileController_CreateAPI();

    Mockito.when(userDAO.find("s3test", "s3testuser"))
        .thenThrow(new DataAccessException("failed to search user.\n"));

    final String expectedResponseBody =
        "<?xml version=\"1.0\" " + "encoding=\"UTF-8\" standalone=\"no\"?>" +
        "<ErrorResponse xmlns=\"https://iam.seagate.com/doc/2010-05-08/\">" +
        "<Error><Code>InternalFailure</Code>" +
        "<Message>The request processing has failed because of an " +
        "unknown error, exception or failure.</Message></Error>" +
        "<RequestId>0000</RequestId>" + "</ErrorResponse>";

    ServerResponse response = userLoginProfileController.create();
    Assert.assertEquals(expectedResponseBody, response.getResponseBody());
    Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        response.getResponseStatus());
  }

  @Test public void
  CreateUserLoginProfile_LoginProfileSaveFailed_ReturnInternalServerError()
      throws Exception {

    createUserLoginProfileController_CreateAPI();

    User user = new User();
    user.setAccountName("s3test");
    user.setName("s3testuser");
    user.setId("123");

    Mockito.when(userDAO.find("s3test", "s3testuser")).thenReturn(user);
    Mockito.doThrow(new DataAccessException("failed to search user.\n"))
        .when(userLoginProfileDAO)
        .save(user);

    final String expectedResponseBody =
        "<?xml version=\"1.0\" " + "encoding=\"UTF-8\" standalone=\"no\"?>" +
        "<ErrorResponse xmlns=\"https://iam.seagate.com/doc/2010-05-08/\">" +
        "<Error><Code>InternalFailure</Code>" +
        "<Message>The request processing has failed because of an " +
        "unknown error, exception or failure.</Message></Error>" +
        "<RequestId>0000</RequestId>" + "</ErrorResponse>";

    ServerResponse response = userLoginProfileController.create();
    Assert.assertEquals(expectedResponseBody, response.getResponseBody());
    Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        response.getResponseStatus());
  }

  @Test public void CreateUser_NewUserCreated_ReturnCreateResponse()
      throws Exception {
    createUserLoginProfileController_CreateAPI();

    User user = new User();
    user.setAccountName("s3test");
    user.setName("s3testuser");
    user.setId("123");

    Mockito.when(userDAO.find("s3test", "s3testuser")).thenReturn(user);
    Mockito.doNothing().when(userDAO).save(user);

    final String expectedResponseBody =
        "<?xml version=\"1.0\" " + "encoding=\"UTF-8\" standalone=\"no\"?>" +
        "<CreateLoginProfileResponse " +
        "xmlns=\"https://iam.seagate.com/doc/2010-05-08/\">" +
        "<CreateLoginProfileResult>" + "<LoginProfile>" +
        "<UserName>s3testuser</UserName>" + "<UserId>123</UserId>" +
        "</LoginProfile>" + "</CreateLoginProfileResult>" +
        "<ResponseMetadata>" + "<RequestId>0000</RequestId>" +
        "</ResponseMetadata>" + "</CreateLoginProfileResponse>";

    ServerResponse response = userLoginProfileController.create();
    Assert.assertEquals(expectedResponseBody, response.getResponseBody());
    Assert.assertEquals(HttpResponseStatus.CREATED,
                        response.getResponseStatus());
  }

  /**
   * Below method will Test successful API response when valid username and
   * password present
   *
   * @throws Exception
   */
  @Test public void GetUserLoginProfile_Sucessful_Api_Response()
      throws Exception {
    User user = new User();
    user.setAccountName(ACCOUNT_NAME);
    user.setName(USERNAME);
    user.setId(USERID);
    user.setPassword("password");
    PowerMockito.mockStatic(DAODispatcher.class);
    PowerMockito.doReturn(mockUserDao)
        .when(DAODispatcher.class, GET_RESOURCE_DAO, DAOResource.USER);
    Mockito.when(mockUserDao.find(ACCOUNT_NAME, USERNAME)).thenReturn(user);
    userLoginProfileController = Mockito.spy(
        new UserLoginProfileController(requestorObj, requestBodyObj));
    ServerResponse response = userLoginProfileController.list();
    Assert.assertEquals(HttpResponseStatus.OK, response.getResponseStatus());
  }

  /**
   * Below method will test DataAccessException when requested user is not
   * present inside LDAP
   *
   * @throws Exception
   */
  @Test public void GetUserLoginProfile_DataAccessException_Response()
      throws Exception {
    PowerMockito.mockStatic(DAODispatcher.class);
    PowerMockito.doReturn(mockUserDao)
        .when(DAODispatcher.class, GET_RESOURCE_DAO, DAOResource.USER);
    Mockito.doThrow(new DataAccessException("failed to search user.\n"))
        .when(mockUserDao)
        .find(ACCOUNT_NAME, USERNAME);
    userLoginProfileController = Mockito.spy(
        new UserLoginProfileController(requestorObj, requestBodyObj));
    ServerResponse response = userLoginProfileController.list();
    Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        response.getResponseStatus());
  }

  /**
   * Below method will test NOSUCHENTITY exception when login profile is not
   * set for requested user inside LDAP
   *
   * @throws Exception
   */
  @Test public void GetUserLoginProfile_NoSuchEntity_Response()
      throws Exception {
    User user = new User();
    user.setAccountName(ACCOUNT_NAME);
    user.setName(USERNAME);
    user.setId(USERID);
    PowerMockito.mockStatic(DAODispatcher.class);
    PowerMockito.doReturn(mockUserDao)
        .when(DAODispatcher.class, GET_RESOURCE_DAO, DAOResource.USER);
    Mockito.when(mockUserDao.find(ACCOUNT_NAME, USERNAME)).thenReturn(user);
    userLoginProfileController = Mockito.spy(
        new UserLoginProfileController(requestorObj, requestBodyObj));
    ServerResponse response = userLoginProfileController.list();
    Assert.assertEquals(HttpResponseStatus.UNAUTHORIZED,
                        response.getResponseStatus());
  }
}