;@author
;Huber Flores

(ns WebApiWrapper.ec2
  (:use [WebApiWrapper core helpers]
        [clj-ssh.ssh])
  (:import (com.xerox.amazonws.ec2 Jec2 ImageDescription LaunchConfiguration))
)

;;
;; Elastic cloud / EC2
;;

(defn list-ec2-images
  "Returns a sequence of maps containing information about all available EC2 images."
  []
  (let [ec2 (Jec2. *aws-id* *aws-key*)]
    (map bean (.describeImages ec2 '()))))

;(def ACCESS_KEY "")
;(def SECRET_KEY "")
;(def USERNAME "")

(def AVAILABILITY_ZONE "")
(def KEY_NAME "")
(def MIN_COUNT 1)
(def MAX_COUNT 1)

(def VM_DEVICE_NAME "/dev/sdf")
(def VM_MOUNT_POINT "/volume1")

(def VM_USERNAME "ubuntu")
(def VM_PRIVATE_KEY_LOCATION "")
(def VM_SUDO "sudo")

(defn read-config
  "Reads 'aws.properties' file and defines variables."
  []
  (let [properties (into {}
    (doto (java.util.Properties.)
      (.load (java.io.FileInputStream. "aws.properties"))))]

    (def ACCESS_KEY (properties "id"))
    (def SECRET_KEY (properties "key"))
    (def USERNAME (properties "username"))

    (def IMAGE_ID (properties "launchconfig.imageID"))
    (def AVAILABILITY_ZONE (properties "launchConfig.availabilityZone"))
    (def KEY_NAME (properties "launchConfig.keyName"))
    (def MIN_COUNT (Integer/parseInt (properties "launchConfig.minCount")))
    (def MAX_COUNT (Integer/parseInt (properties "launchConfig.maxCount")))

    (def VM_DEVICE_NAME (properties "attachVolume.deviceName"))
    (def VM_MOUNT_POINT (properties "attachVolume.mountPoint"))

    (def VM_USERNAME (properties "vmAuth.username"))
    (def VM_PRIVATE_KEY_LOCATION (properties "vmAuth.privateKeyLocation"))
    (def VM_SUDO (properties "vmAuth.sudo"))))

(defn run-instance
  "Start a new instance on EC2"
  [aws-id aws-key image-id]
  (let [launchconfig (LaunchConfiguration. image-id)
        ec2 (Jec2. aws-id aws-key)]
    (.setAvailabilityZone launchconfig AVAILABILITY_ZONE)
    (.setKeyName launchconfig KEY_NAME)
    (.setMinCount launchconfig MIN_COUNT)
    (.setMaxCount launchconfig MAX_COUNT)
    (let [reservation-desc (.runInstances ec2 launchconfig)
          instance (.get (.getInstances reservation-desc) 0)
          instances (map identity [(.getInstanceId instance)])]
      (loop [x true]
        (when (= x true)
          (let [instance2 (.get (.getInstances
                    (.get (.describeInstances ec2 instances) 0)) 0)]
            (println (str
                      "Create: Instance ID = " (.getInstanceId instance2)
                      " ; State = " (.getState instance2)))
            (if (= true (.isRunning instance2))
              (recur false)
              (do
                (Thread/sleep 5000)
                (recur true))))))
      (Thread/sleep 30000)
      (.getInstanceId
        (.get (.getInstances (.get (.describeInstances ec2 instances) 0)) 0)))))

(defn terminate-instance
  "Terminates an existing instance."
  [aws-id aws-key instance-id]
  (let [ec2 (Jec2. aws-id aws-key)
        instances (map identity [instance-id])]
    (try (.terminateInstances ec2 instances)
      (catch Exception e ()))
    (println (str "Terminate: Instance ID = " instance-id))
    (str instance-id)))

(defn create-volume
  "Creates a new EBS volume."
  [aws-id aws-key]
  (let [ec2 (Jec2. aws-id aws-key)
        volume-info (.createVolume ec2 "2" nil AVAILABILITY_ZONE)
        volumes (map identity [(.getVolumeId volume-info)])]
    (println "Creating a new volume ...")
    (loop [x true]
      (when (= x true)
        (let [vol-info (.get (.describeVolumes ec2 volumes) 0)]
          (println (str "Create: Volume ID = " (.getVolumeId vol-info)
            " ; State = " (.getStatus vol-info)))
          (if (= true
            (.equals "available" (.toLowerCase (.getStatus vol-info))))
            (recur false)
            (do
              (Thread/sleep 2000)
              (recur true))))))
    (.getVolumeId (.get (.describeVolumes ec2 volumes) 0))))

(defn delete-volume
  "Deletes a detached volume"
  [aws-id aws-key volume-id]
  (let [ec2 (Jec2. aws-id aws-key)]
    (println (str "Delete Volume ID = " volume-id))
    (.deleteVolume ec2 volume-id)
    (str volume-id)))

(defn attach-volume
  "Attaches an existing volume to an instance"
  [aws-id aws-key volume-id instance-id]
  (let [ec2 (Jec2. aws-id aws-key)
        volumes (map identity [volume-id])]
    (println "Attaching volume" volume-id "to instance" instance-id)
    (.attachVolume ec2 volume-id instance-id VM_DEVICE_NAME)
    (loop [x true]
      (when (= x true)
        (let [vol-info (.get (.getAttachmentInfo
          (.get (.describeVolumes ec2 volumes) 0)) 0)]
          (println "Attach: Volume ID =" (.getVolumeId vol-info)
                   "; State =" (.getStatus vol-info))
          (if (= true (.equals "attached" (.toLowerCase (.getStatus vol-info))))
            (recur false)
            (do
              (Thread/sleep 2000)
              (recur true))))))))

(defn detach-volume
  "Detaches an attached volume from an instance"
  [aws-id aws-key volume-id instance-id]
  (let [ec2 (Jec2. aws-id aws-key)
        volumes (map identity [volume-id])]
    (println "Detaching volume" volume-id "from instance" instance-id)
    (.detachVolume ec2 volume-id instance-id VM_DEVICE_NAME false)
    (loop [x true]
      (when (= x true)
        (let [vol-info (.get (.describeVolumes ec2 volumes) 0)]
          (println "Detach: Volume ID =" (.getVolumeId vol-info)
                   "; State =" (.getStatus vol-info))
          (if (= true
            (.equals "available" (.toLowerCase (.getStatus vol-info))))
            (recur false)
            (do
              (Thread/sleep 2000)
              (recur true))))))))

(defn mount-volume
  "Mounts an attached volume on an instance."
  [aws-id aws-key instance-id]
  (let [ec2 (Jec2. aws-id aws-key)
        instance (.get (.getInstances (.get
          (.describeInstances ec2 (map identity [instance-id])) 0)) 0)]
    (println (str "Mounting volume on instance " instance-id))
    (let [agent (ssh-agent {:use-system-ssh-agent false})]
      (add-identity agent {:private-key-path VM_PRIVATE_KEY_LOCATION})
      (let [session (session agent (.getDnsName instance)
        {:username VM_USERNAME :strict-host-key-checking :no})]
        (with-connection session
          (let [result (ssh session {:in
            (str "sudo mkfs.ext3 -F -q " VM_DEVICE_NAME "; "
                 "sudo mkdir " VM_MOUNT_POINT "; "
                 "sudo mount " VM_DEVICE_NAME " " VM_MOUNT_POINT)})]
            (println (result :out))))))))

(defn unmount-volume
  "Unmounts mounted volume on an instance for detaching."
  [aws-id aws-key instance-id]
  (let [ec2 (Jec2. aws-id aws-key)
        instance (.get (.getInstances
          (.get (.describeInstances ec2 (map identity [instance-id])) 0)) 0)]
    (println (str "Unmounting volume on instance " instance-id))
    (let [agent (ssh-agent {:use-system-ssh-agent false})]
      (add-identity agent {:private-key-path VM_PRIVATE_KEY_LOCATION})
      (let [session (session agent (.getDnsName instance)
            {:username VM_USERNAME :strict-host-key-checking :no})]
        (with-connection session
          (let [result (ssh session {:in (str "sudo umount " VM_MOUNT_POINT)})]
            (println (result :out))))))))
