/*
 * COPYRIGHT 2016 SEAGATE LLC
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
 * Original author:  Kaustubh Deorukhkar <kaustubh.deorukhkar@seagate.com>
 * Original creation date: 16-Jun-2016
 */

#pragma once

#ifndef __MERO_FE_S3_SERVER_S3_CLI_OPTIONS_H__
#define __MERO_FE_S3_SERVER_S3_CLI_OPTIONS_H__

#include <gflags/gflags.h>
#include <glog/logging.h>

DECLARE_string(s3host);
DECLARE_int32(s3port);

DECLARE_string(s3loglevel);

DECLARE_bool(perfenable);
DECLARE_string(perflogfile);

DECLARE_string(clovislocal);
DECLARE_string(clovisha);
DECLARE_string(clovisconfd);
DECLARE_int32(clovislayoutid);
DECLARE_string(clovisprofile);

DECLARE_string(authhost);
DECLARE_int32(authport);
DECLARE_bool(disable_auth);

DECLARE_bool(fake_authenticate);
DECLARE_bool(fake_authorization);

DECLARE_bool(fake_clovis_createobj);
DECLARE_bool(fake_clovis_writeobj);
DECLARE_bool(fake_clovis_deleteobj);
DECLARE_bool(fake_clovis_createidx);
DECLARE_bool(fake_clovis_deleteidx);
DECLARE_bool(fake_clovis_getkv);
DECLARE_bool(fake_clovis_putkv);
DECLARE_bool(fake_clovis_deletekv);
DECLARE_bool(fault_injection);

// Loads config and also processes the command line options.
// options specified on cli overrides the option specified in config file.
int parse_and_load_config_options(int argc, char ** argv);

// Cleanups related to options processing.
void finalize_cli_options();

#endif
