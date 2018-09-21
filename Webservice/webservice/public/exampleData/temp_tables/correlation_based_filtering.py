#!/usr/bin/python

import pdb

import pandas as pd
import numpy as np
import locale
from sklearn import metrics
from scipy.stats import pearsonr
from scipy.stats import f_oneway
import pdb
import numpy as np; np.random.seed(0)
import seaborn as sns; sns.set()
import matplotlib.pyplot as plt
import json
import jellyfish
import copy
import math
import re
import sys  
reload(sys)  
sys.setdefaultencoding('utf8')


# ============================================================================
# Functions
# ============================================================================

def replace_list_item(the_list, original_value, replacement_value):
    for index, value in enumerate(the_list):
        if isinstance(value, str):
            if value.lower() == original_value.lower():
                the_list[index] = replacement_value
    return the_list


def replace_null_values(column):
    column = replace_list_item(column, 'null', None)
    column = replace_list_item(column, 'nil', None)
    column = replace_list_item(column, 'none', None)
    column = replace_list_item(column, '-', None)
    column = replace_list_item(column, 'not available', None)
    
    return column


def best_conversion_score(column):
    converted_columns = []
    locale.setlocale(locale.LC_ALL, 'en_GB')
    converted_columns += [as_numbers(column)]
    converted_columns += [as_numbers__bad_characters_removed(column)]
    locale.setlocale(locale.LC_ALL, 'de_DE')
    converted_columns += [as_numbers(column)]
    converted_columns += [as_numbers__bad_characters_removed(column)]
    
    best_conversion = sorted(converted_columns, key=lambda tup: tup[1])[0]
    
    return best_conversion[1]

    
def remove_non_numerical_chars(column):
    copy_of_column = copy.deepcopy(column)
    for index in range(len(column)):
        if pd.notnull(column[index]):
            column[index] = re.sub("[^0-9\.,]", "", copy_of_column[index])
    return column
    
def convert_column(column):
    if best_conversion_score(column) < 0.4:
        column = remove_non_numerical_chars(column)
    
    converted_columns = []
    locale.setlocale(locale.LC_ALL, 'en_GB')
    converted_columns += [as_numbers(column)]
    converted_columns += [as_numbers__bad_characters_removed(column)]
    locale.setlocale(locale.LC_ALL, 'de_DE')
    converted_columns += [as_numbers(column)]
    converted_columns += [as_numbers__bad_characters_removed(column)]
    
    best_conversion = sorted(converted_columns, key=lambda tup: tup[1])[0]
    
    
    if best_conversion[1]< 0.1:
        return best_conversion[0]
    else:
        return column
    


def as_numbers(column):
    number_of_exceptions = 0
    number_of_non_null = 0
    new_column = []
    for value in column:
        if not value is np.nan and not value is None:
            number_of_non_null += 1
            try:
                new_value = locale.atof(value)
            except:
                new_value = value
                number_of_exceptions += 1
        
        else:
            new_value = value
                
        new_column += [new_value]
        
    exception_percentage = float(number_of_exceptions)/(number_of_non_null+1)
    return (new_column,exception_percentage)


def as_numbers__bad_characters_removed(column):
    number_of_exceptions = 0
    number_of_non_null = 0
    new_column = []
    for value in column:
        if isinstance(value, basestring):
            value = value.replace(' ','').replace('*','')
            
        if not value is np.nan and not value is None:
            number_of_non_null += 1
            try:
                new_value = locale.atof(value)
            except:
                new_value = value
                number_of_exceptions += 1
        
        else:
            new_value = value
                
        new_column += [new_value]
        
    exception_percentage = float(number_of_exceptions)/(number_of_non_null+1)
    return (new_column,exception_percentage)


    def value_selection(fields_values):
        """This value selection algorithm works with similarity based voting."""
        if len(fields_values)>0:
            value_scores_df = pd.DataFrame()

            for index, value in enumerate(fields_values):
                other_values = copy.deepcopy(fields_values)
                other_values.pop(index)
                score = 0
                for other_value in other_values:
                    score += jellyfish.jaro_distance(unicode(value), unicode(other_value))
                new_row = pd.DataFrame({'value':value, 'score':score}, index=[index])
                value_scores_df = value_scores_df.append(new_row)

            selected_value = value_scores_df.sort_values('score', ascending=False).iloc[0,1]
            return selected_value
        else:
            return ''

def remove_rows_with_null_values(table):
    columns = table.columns
    null_ness_0 = pd.notnull(table[columns[0]])
    good_rows_0 = null_ness_0[null_ness_0==True].index
    null_ness_1 = pd.notnull(table[columns[1]])
    good_rows_1 = null_ness_1[null_ness_1==True].index
    good_rows = list(set(good_rows_0).intersection(set(good_rows_1)))
    table = table.ix[good_rows,:]
    return table

def anova_correlation(table):
    columns = table.columns
    categories = table.ix[:,0].unique()
    category_values = []
    category_with_multiple_values = False
    for category in categories:
        category_values += [list(table.ix[table[columns[0]]==category, columns[1]])]
        if len(list(table.ix[table[columns[0]]==category, columns[1]]))> 1:
            category_with_multiple_values = True
    
    if category_with_multiple_values:
        (f_statistic, p_value) = f_oneway(*category_values)
#         correlation = 1 - p_value
        correlation = (1-p_value)* 0.5 + 0.3*math.exp(-10*p_value)
    else:
        correlation = 0.0
    
    return  correlation

def mutual_information(table):
    columns = table.columns
    column1 = list(table[columns[0]])
    column2 = list(table[columns[1]])
    correlation = metrics.adjusted_mutual_info_score(column1, column2)
    return correlation

def bens_mutual_information(table):
    
    columns = table.columns
    mutual_information = 0
    for value0 in table[columns[0]].unique():
        for value1 in table[columns[0]].unique():
            p_x_y = float(len(table[np.logical_and(table[columns[0]]==value0,table[columns[1]]==value1)]))/len(table)
            p_x = float(len(table[table[columns[0]]==value0]))/len(table)
            p_y = float(len(table[table[columns[1]]==value1]))/len(table)
            if ((p_x * p_y) > 0.0) and (p_x_y > 0.0):
                mutual_information += p_x_y * math.log(p_x_y/(p_x * p_y))
    return mutual_information

def pearson_correlation_coeff(table):
    columns = table.columns
    column1 = list(table[columns[0]])
    column2 = list(table[columns[1]])
    pearsons_correlation_coeff, p_value = pearsonr(column1, column2)
    return abs(pearsons_correlation_coeff)

def numeric_percentage(table_column):
    column_values = [el for el in table_column.tolist() if not pd.isnull(el) ]
    numeric_characters = ['0','1','2','3','4','5','6','7','8','9','.',',']
    numeric_count = 0
    total_count = 0
    for value in list(column_values):
        for char in str(value):
            total_count += 1
            if char in numeric_characters:
                numeric_count += 1
    
    return float(numeric_count)/total_count

def remove_non_numeric_columns(correlation_matrix_df, column_datatypes ):
    
    for column_number, column_name in enumerate(correlation_matrix_df.columns):
        if fused_table.dtypes[column_number]== 'O':
            if column_name in correlation_matrix_df.columns:
                correlation_matrix_df = correlation_matrix_df.drop(column_name, axis=0)
                correlation_matrix_df = correlation_matrix_df.drop(column_name, axis=1)
            
    return correlation_matrix_df

    
# ============================================================================
# Main
# ============================================================================

if __name__ == "__main__":
    # correlation_attribute = sys.argv[1]
    correlation_threshold = float(sys.argv[1])
    subject_column = sys.argv[2]
    correlation_attribute = sys.argv[3]

    print "step1"
    original_table_path = "public/exampleData/temp_tables/temp_table1.csv"
    unconstrained_table_path = "public/exampleData/temp_tables/temp_table2.csv"
    filtered_table_path = "public/exampleData/temp_tables/temp_table3.csv"

    original_table = pd.read_csv(original_table_path, sep="\t", encoding='utf-8')
    unconstrained_table = pd.read_csv(unconstrained_table_path, sep="\t", encoding='utf-8')
    print list(unconstrained_table.columns)


    for column_name in unconstrained_table.columns:
        column_values = unconstrained_table[column_name]
        column_values = replace_null_values(column_values)
        column_values = convert_column(column_values)
        unconstrained_table[column_name] = column_values
    print "step2"
    correlation_table = pd.DataFrame()

    lower_case_columns = [col.lower() for col in unconstrained_table.columns]
    print lower_case_columns
    subject_column_index = lower_case_columns.index(subject_column.lower())
    subject_column = list(unconstrained_table.columns)[subject_column_index]
    column_number1 = lower_case_columns.index(correlation_attribute.lower())
    column_name1 = list(unconstrained_table.columns)[column_number1]
    print "step4"
    for column_number2, column_name2 in enumerate(unconstrained_table.columns):
        print "%s/%s" % (column_number2, len(unconstrained_table.columns))
        print "%s <--> %s" % (column_name1, column_name2)
        if column_number1 != column_number2:
            dtype1 = unconstrained_table.dtypes[column_number1]
            dtype2 = unconstrained_table.dtypes[column_number2]

            fused_sub_table = unconstrained_table[[column_name1, column_name2]]
            fused_sub_table = remove_rows_with_null_values(fused_sub_table)
            
            if dtype1=='O' and dtype2=='float64':
                correlation = anova_correlation(fused_sub_table[[column_name1, column_name2]])
            elif dtype1=='float64' and dtype2=='O':
                correlation = anova_correlation(fused_sub_table[[column_name2, column_name1]])
            elif dtype1=='O' and dtype2=='O':
                correlation = 0.
                # if numeric_percentage(unconstrained_table[column_name1]) < 0.5 and numeric_percentage(unconstrained_table[column_name2]) < 0.5:
                #     correlation = mutual_information(fused_sub_table[[column_name1, column_name2]])
                # else:
                #     correlation = 0.0
            elif dtype1=='float64' and dtype2=='float64':
                correlation = pearson_correlation_coeff(fused_sub_table[[column_name1, column_name2]])

            if np.isnan(correlation) or len(fused_sub_table)<2:
                correlation = 0.0
            correlation = abs(correlation)
            
            new_row = pd.DataFrame({'column_number1':column_number1, 'column_name1':column_name1, 'column1_dtype': dtype1,  'column_number2':column_number2, 'column_name2':column_name2, 'column2_dtype': dtype2, 'correlation':correlation, 'number_of_valid_rows':len(fused_sub_table)}, index=[0])
            correlation_table = correlation_table.append(new_row)

    print "step5"
    # pdb.set_trace()
    correlating_columnnames = correlation_table.ix[correlation_table['correlation'] >= correlation_threshold,'column_name2']
    correlating_columnnames = list(correlating_columnnames)
    print correlating_columnnames
    correlating_columnnames = [el for el in correlating_columnnames if el not in [subject_column,correlation_attribute]]
    correlating_columnnames = [subject_column,correlation_attribute] + correlating_columnnames
    print unconstrained_table.shape
    print correlating_columnnames
    unconstrained_table[correlating_columnnames].to_csv(filtered_table_path, index=False, sep="\t")
    locale.setlocale(locale.LC_ALL)
    print "end"

