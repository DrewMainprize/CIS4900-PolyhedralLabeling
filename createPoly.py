import sys, os
from colorama import init as colorama_init
from colorama import Fore
from colorama import Style

numPlanes = 10
probVisible = 1.0
exeString = "polyhedral-scene-generator-main/poly_scene_1_7.exe " + str(numPlanes) + " " + str(probVisible)

# Initialize terminal colours
colorama_init()
print(f"{Fore.GREEN}")

# Use command line args for the number of polynomials to generate

if len(sys.argv) < 2:
    print(f"{Fore.RED}ERROR: Please input number of polyhedrals to create as an arg")
    print("Example: python3 createPoly.py 100")
    exit()

print("Creating ", sys.argv[1], " polyhedrals")

os.system(os.path.abspath(exeString))
