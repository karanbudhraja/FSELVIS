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
        axes = plt.gca()
        axes.set_xlim([1, 8])
        axes.set_ylim([1, 8])

        X = numpy.arange(1,sellerCount+1)
        Y = numpy.arange(1,sellerCount+1)
        X, Y = numpy.meshgrid(X, Y)
        Z = witnessParticipationData
        #plot = ax.scatter(X, Y, Z, cmap=cm.coolwarm)
        plot = ax.plot_wireframe(X, Y, Z, color="black")
        plot = ax.contourf(X, Y, Z, cmap=cm.coolwarm)
        ax.set_xlabel('Requester Number')
        ax.set_ylabel('Witness Number')
        ax.set_zlabel('Participation Count')
        plt.savefig(fileName + ".png")
        plt.close(fig)
