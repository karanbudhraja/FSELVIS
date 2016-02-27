import numpy
import matplotlib.pyplot as plt

binarySearchData = numpy.loadtxt("learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plt.plot(binarySearchData[:,-2])

#learningWithBinarySearchData = numpy.loadtxt("learningTest_.txt", delimiter=",", skiprows=1)
#learningWithBinarySearchData = numpy.flipud(learningWithBinarySearchData)
#plt.plot(learningWithBinarySearchData[:,-2])

plt.show()
