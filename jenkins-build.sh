#!/bin/sh

set -xe

export PATH=/opt/seagate/s3/bin/:$PATH
echo $PATH

#git clone --recursive http://es-gerrit.xyus.xyratex.com:8080/s3server

S3_BUILD_DIR=`pwd`

# Set up the python virtualenv for System tests
cd st/clitests
./setup.sh
cd $S3_BUILD_DIR

systemctl stop s3authserver || echo "Cannot stop s3authserver services"

# nginx used as reverse-proxy for s3 service.
systemctl restart nginx || echo "Cannot restart nginx service."

# Check if mero build is cached and is latest, else rebuild mero as well
THIRD_PARTY=$S3_BUILD_DIR/third_party
MERO_SRC=$THIRD_PARTY/mero
BUILD_CACHE_DIR=$HOME/.seagate_src_cache

cd $MERO_SRC && current_mero_rev=`git rev-parse HEAD` && cd $S3_BUILD_DIR
cached_mero_rev=`cat $BUILD_CACHE_DIR/cached_mero.git.rev` || echo "Mero not cached at $BUILD_CACHE_DIR"

if [ "$current_mero_rev" != "$cached_mero_rev" ]
then
  # we need to rebuild mero, clean old cache
  rm -rf $BUILD_CACHE_DIR
fi

./rebuildall.sh --no-mero-rpm --use-build-cache

# Stop any old running mero
cd $MERO_SRC
echo "Stopping any old running mero services"
./m0t1fs/../clovis/st/utils/mero_services.sh stop || echo "Cannot stop mero services"
cd $S3_BUILD_DIR

# Stop any old running S3 instances
echo "Stopping any old running s3 services"
./dev-stops3.sh

# Clean up mero and S3 log and data dirs
rm -rf /mnt/store/mero/* /var/log/mero/* /var/mero/* /var/log/seagate/s3/* /var/log/seagate/auth/*

# Start mero for new tests
cd $MERO_SRC
echo "Starting new built mero services"
./m0t1fs/../clovis/st/utils/mero_services.sh start
cd $S3_BUILD_DIR

# Start S3 auth
echo "Starting new built s3 auth services"
systemctl restart s3authserver

# Start S3 gracefully, with max 3 attempts
echo "Starting new built s3 services"
retry=1
max_retries=3
statuss3=0
while [[ $retry -le $max_retries ]]
do
  statuss3=0
  ./dev-starts3.sh
  # Give it a second to start
  sleep 1
  ./iss3up.sh 1 || statuss3=$?

  if [ "$statuss3" == "0" ]; then
    echo "S3 service started successfully..."
    break
  else
    # Sometimes if mero is not ready, S3 may fail to connect
    # cleanup and restart
    ./dev-stops3.sh
    sleep 1
  fi
  retry=$((retry+1))
done

if [ "$statuss3" != "0" ]; then
  echo "Cannot start S3 service"
  tail -50 /var/log/seagate/s3/s3server.INFO
  exit 1
fi

# Run Unit tests and System tests
./runalltest.sh --no-mero-rpm || (tail -50 /var/log/seagate/s3/s3server.INFO && exit 1)

# To debug if there are any errors
tail -50 /var/log/seagate/s3/s3server.ERROR || echo "No Errors"

systemctl stop s3authserver || echo "Cannot stop s3authserver services"

./dev-stops3.sh || echo "Cannot stop s3 services"

cd $MERO_SRC
./m0t1fs/../clovis/st/utils/mero_services.sh stop || echo "Cannot stop mero services"
cd $S3_BUILD_DIR