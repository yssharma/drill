/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.server.rest.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.drill.exec.proto.UserBitShared.CoreOperatorType;
import org.apache.drill.exec.proto.UserBitShared.MajorFragmentProfile;
import org.apache.drill.exec.proto.UserBitShared.MinorFragmentProfile;
import org.apache.drill.exec.proto.UserBitShared.OperatorProfile;
import org.apache.drill.exec.proto.UserBitShared.QueryProfile;
import org.apache.drill.exec.proto.helper.QueryIdHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProfileWrapper {

  public QueryProfile profile;
  public String id;

  public ProfileWrapper(final QueryProfile profile) {
    this.profile = profile;
    this.id = QueryIdHelper.getQueryId(profile.getId());
  }

  public boolean hasError() {
    return profile.hasError() && profile.getError() != null;
  }

  public QueryProfile getProfile() {
    return profile;
  }

  public String getId() {
    return id;
  }

  public String getQueryId() {
    return QueryIdHelper.getQueryId(profile.getId());
  }

  public List<OperatorWrapper> getOperatorProfiles() {
    final List<OperatorWrapper> ows = Lists.newArrayList();
    final Map<ImmutablePair<Integer, Integer>, List<ImmutablePair<OperatorProfile, Integer>>> opmap = Maps.newHashMap();

    final List<MajorFragmentProfile> majors = new ArrayList<>(profile.getFragmentProfileList());
    Collections.sort(majors, Comparators.majorIdCompare);
    for (final MajorFragmentProfile major : majors) {

      final List<MinorFragmentProfile> minors = new ArrayList<>(major.getMinorFragmentProfileList());
      Collections.sort(minors, Comparators.minorIdCompare);
      for (final MinorFragmentProfile minor : minors) {

        final List<OperatorProfile> ops = new ArrayList<>(minor.getOperatorProfileList());
        Collections.sort(ops, Comparators.operatorIdCompare);
        for (final OperatorProfile op : ops) {

          final ImmutablePair<Integer, Integer> ip = new ImmutablePair<>(
              major.getMajorFragmentId(), op.getOperatorId());
          if (!opmap.containsKey(ip)) {
            final List<ImmutablePair<OperatorProfile, Integer>> l = Lists.newArrayList();
            opmap.put(ip, l);
          }
          opmap.get(ip).add(new ImmutablePair<>(op, minor.getMinorFragmentId()));
        }
      }
    }

    final List<ImmutablePair<Integer, Integer>> keys = new ArrayList<>(opmap.keySet());
    Collections.sort(keys);

    for (final ImmutablePair<Integer, Integer> ip : keys) {
      ows.add(new OperatorWrapper(ip.getLeft(), opmap.get(ip)));
    }

    return ows;
  }

  public List<FragmentWrapper> getFragmentProfiles() {
    final List<FragmentWrapper> fws = Lists.newArrayList();

    final List<MajorFragmentProfile> majors = new ArrayList<>(profile.getFragmentProfileList());
    Collections.sort(majors, Comparators.majorIdCompare);
    for (final MajorFragmentProfile major : majors) {
      fws.add(new FragmentWrapper(major, profile.getStart()));
    }

    return fws;
  }

  public String getFragmentsOverview() {
    TableBuilder tb = new TableBuilder(FragmentWrapper.FRAGMENT_OVERVIEW_COLUMNS);
    for (final FragmentWrapper fw : getFragmentProfiles()) {
      fw.addSummary(tb);
    }
    return tb.toString();
  }



  public String getOperatorsOverview() {
    final TableBuilder tb = new TableBuilder(OperatorWrapper.OPERATORS_OVERVIEW_COLUMNS);
    for (final OperatorWrapper ow : getOperatorProfiles()) {
      ow.addSummary(tb);
    }
    return tb.toString();
  }

  public String getOperatorsJSON() {
    final StringBuilder sb = new StringBuilder("{");
    String sep = "";
    for (final CoreOperatorType op : CoreOperatorType.values()) {
      sb.append(String.format("%s\"%d\" : \"%s\"", sep, op.ordinal(), op));
      sep = ", ";
    }
    return sb.append("}").toString();
  }


}
