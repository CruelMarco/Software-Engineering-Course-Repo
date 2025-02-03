"""
This file runs some basic tests for Assignment04.

You should run this on your implementation. For a correct implementation all tests should pass.
"""
from Assignment04 import get_name, get_matriculation_number, get_cart, get_error_rate, \
    get_optimal_configuration, get_performance, get_pim

import findimports

# Sanity checks:
print(f"Your name: {get_name()}")
print(f"Your matriculation number: {get_matriculation_number()}")

imports, unused_imports = findimports.find_imports_and_track_names("Assignment04.py")
import_names = {iminfo.name.split('.')[0] for iminfo in imports} - {iminfo.name.split('.')[0] for iminfo in unused_imports}
allowed_imports = {"pandas", "typing", "itertools"}

if not import_names.issubset(allowed_imports):
    print("Import check: Failed")
    print(f"Your submission includes the following additional imports: {import_names-allowed_imports}")
else:
    print("Import check: Passed")

# Task 1
test_cart = {'error_of_split': 24.0,
                'mean': 764.4,
                'name': 'X',
                'split_by_feature': 'segmentation',
 'successor_left': {'error_of_split': 0.0,
                    'mean': 774.0,
                    'name': 'XL',
                    'split_by_feature': '1gb',
                    'successor_left': {'error_of_split': None,
                                       'mean': 770.0,
                                       'name': 'XLL',
                                       'split_by_feature': None,
                                       'successor_left': None,
                                       'successor_right': None},
                    'successor_right': {'error_of_split': None,
                                        'mean': 776.0,
                                        'name': 'XLR',
                                        'split_by_feature': None,
                                        'successor_left': None,
                                        'successor_right': None}},
 'successor_right': {'error_of_split': None,
                     'mean': 750.0,
                     'name': 'XR',
                     'split_by_feature': None,
                     'successor_left': None,
                     'successor_right': None}}

student_cart = get_cart("Performance_01.csv")

if student_cart == test_cart:
    print("Task 1: passed")
else:
    print("Task 1: failed")

# Task 2
test_pim = {
    "": 100,
    "A": 20,
    "B": 30,
    "C": -20,
    "A*B": -15,
    "B*C": 40,
    "B*D": 10,
    "A*B*C": -45
}

student_pim = get_pim("Performance_02.csv")
if student_pim == test_pim:
    print("Task 2: passed")
else:
    print("Task 2: failed")

# Task 3a
student_prediction = get_performance(test_cart, {"secompress", "segmentation"})
if student_prediction == 776.0:
    print("Task 3a: passed")
else:
    print("Task 3a: failed")

# Task 3b
student_error = get_error_rate(test_cart, "Performance_03b.csv")
if student_error == 3.8:
    print("Task 3b: passed")
else:
    print("Task 3b: failed")

# Task 3c
feature_model = {
  "name": "secompress",
  "groupType" : "And",
  "children": [
    {
      "name": "encryption",
      "featureType": "Opt",
      "groupType": "Xor",
      "children": [
        {
          "name": "aes"
        },
        {
          "name": "blowfish"
        }
      ]
    },
    {
      "name": "algorithm",
      "featureType": "Mand",
      "groupType": "Xor",
      "children": [
        {
          "name": "rar"
        },
        {
          "name": "zip"
        }
      ]
    },
    {
      "name": "signature",
      "featureType": "Opt"
    },
    {
      "name": "timestamp",
      "featureType": "Opt"
    },
    {
      "name": "segmentation",
      "featureType": "Opt",
      "groupType": "Or",
      "children": [
        {
          "name": "100mb"
        },
        {
          "name": "1gb"
        }
      ]
    }
  ]
}

partial_config = {"secompress",
                  "encryption",
                  "aes",
                  "segmentation"
                 }

pim  = {
    "": 700,
    "aes": 40,
    "blowfish": 20,
    "rar": 25,
    "zip": 35,
    "signature": 20,
    "timestamp": 5,
    "100mb": 50,
    "1gb": 25,
    "aes*rar" : -45,
    "aes*zip": 25,
    "blowfish*zip": 30,
    "blowfish*rar": -5,
    "blowfish*signature": 15,
    "blowfish*segmentation": 90,
    "aes*signature": -25,
    "aes*timestamp": 100,
    "100mb*aes": 20,
    "1gb*aes": -35
}
optimal_config, performance = get_optimal_configuration(pim, feature_model, partial_config)
reference = {"secompress", "encryption", "aes", "algorithm", "rar", "signature", "segmentation", "1gb"}

if optimal_config == reference and performance == 705:
    print("Task 3c: passed")
else:
    print("Task 3c: failed")
