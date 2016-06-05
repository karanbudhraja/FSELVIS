import numpy
import matplotlib.pyplot as plt

xValues = [x/20.0 for x in range(21)]

means = []
for i in range(0,21,1):
    fileName = "outFolder/1_learningTest" + "_" + str(float(i)/20).replace(".", "") + ".txt"
    binarySearchData = numpy.loadtxt(fileName, delimiter=",", skiprows=1)
    utilityData = binarySearchData[-2]
    means.append(numpy.mean(utilityData))
a00, = plt.plot(xValues, means, "b--")
last = means[-1]

means = []
for i in range(0,21,1):
    fileName = "outFolder/2_learningTest" + "_" + str(float(i)/20).replace(".", "") + ".txt"
    binarySearchData = numpy.loadtxt(fileName, delimiter=",", skiprows=1)
    utilityData = binarySearchData[-2]
    means.append(numpy.mean(utilityData))
a10, = plt.plot(xValues, means, "b-")
last = means[-1]

means = []
for i in range(0,21,1):
    fileName = "outFolder/3_learningTest" + "_" + str(float(i)/20).replace(".", "") + ".txt"
    binarySearchData = numpy.loadtxt(fileName, delimiter=",", skiprows=1)
    utilityData = binarySearchData[-2]
    means.append(numpy.mean(utilityData))
a10ks8, = plt.plot(xValues, means, "b-s")

plt.legend([a00, a10, a10ks8], ["alpha=0.0,Ks=4", "alpha=1.0,Ks=4", "alpha=1.0,Ks=8"], loc="best")
plt.ylabel('Seller Utility')
plt.xlabel('Witness Score Threshold')
plt.show()

