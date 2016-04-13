# ECG Biolock 

#### Dependencies installation
Open terminal and run following commands
```
>>> sudo apt-get update
>>> sudo apt-get upgrade
>>> sudo apt-get install python-pip python-dev
>>> sudo apt-get install python-numpy python-scipy
>>> sudo pip install --upgrade https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-0.7.1-cp27-none-linux_x86_64.whl
>>> sudo pip install -U scikit-learn
>>> sudo pip install skflow
```
#### Model training
Download biolock.tar.gz extract. Than load .csv files with your ecg data from your google drive into ./biolock/ecg_data/user/
In terminal run python script
```
>> python ./biolock/biolock_train_model.py
```
Upload .csv files from ./biolock/dnn_models to your google drive