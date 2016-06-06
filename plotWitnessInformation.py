import numpy
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm

for i in range(0,21,1):
    fileName = "outFolder/1_witnessParticipationData" + "_" + str(float(i)/20).replace(".", "") + ".txt"
    with open(fileName, "r") as dataFile:
        witnessParticipationData = eval(dataFile.readline()[:-1])
        sellerCount = len(witnessParticipationData)**0.5
        witnessParticipationData = numpy.reshape(witnessParticipationData, [sellerCount, sellerCount])
  
        # plot surface
        fig = plt.figure()
        ax = fig.add_subplot(111, projection='3d')
        X = numpy.arange(1,sellerCount+1)
        Y = numpy.arange(1,sellerCount+1)
        X, Y = numpy.meshgrid(X, Y)
        Z = witnessParticipationData
        surf = ax.scatter(X, Y, Z, cmap=cm.coolwarm)
        plt.show()


