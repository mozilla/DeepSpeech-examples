import tensorflow as tf

print ("hello")
av = tf.test.is_gpu_available()
print(av)

av2= tf.config.list_physical_devices('GPU')
print(av2)

#[PhysicalDevice(name='/physical_device:GPU:0', device_type='GPU')]
