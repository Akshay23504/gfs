cd ../..
newDir="chunkServer-"$(date "+%Y%m%d%s")
mkdir $newDir
unzip gfs-1.0.zip -d $newDir
cd $newDir/gfs-1.0/bin
echo $1
sh gfs -Dhttp.port=$1 &