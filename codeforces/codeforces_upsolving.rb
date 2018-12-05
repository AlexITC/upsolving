#
# This code allows you to get a list of the problems you have tried in codeforces
#
# You may need to install mechanize gem, do it with this command:
#  gem install mechanize
#
# To run the code use the command:
#  ruby codeforces_upsolving.rb
#
# Beta version
#  problems from rounds seems to work properly
#  problems from gym are not all retrieved by some reason
#
# @author: Alexis Hernández
#

require 'mechanize'
require 'json'

# Main class
class Upsolving
  def initialize
    # mechanize agent
    @agent = Mechanize.new { |agent| agent.user_agent_alias = 'Mac Safari' }
  end

  # login to codeforces
  def do_login(user, password)
    url = 'http://codeforces.com/enter'
    page = @agent.get(url)
    form = page.forms[1]
    form['handleOrEmail'] = user
    form['password'] = password
    page = form.submit
    fail('User or password is wrong, try again') if page.title == 'Login - Codeforces'
    @token = page.parser.xpath("//html//head//meta")[1].to_s[35..66]

    puts 'Logged in'
  end

  def load_contest_data
    result = @agent.post(
      'http://codeforces.com/data/contests',
      action: 'getSolvedProblemCountsByContest',
      csrf_token: @token
    )

    data = JSON.parse(result.body) # ["solvedProblemCountsByContestId", "problemCountsByContestId"]
    puts 'Contest data loaded'
    data
  end

  def split_contest_data(data)
    gym = {}
    round = {}
    data["solvedProblemCountsByContestId"].each do |key, value|
      entry = {
        solved: value,
        total: data['problemCountsByContestId'][key]
      }
      if key.size <= 3
        round[key] = entry
      elsif key.size == 6 && key[0..2] == '100'
        gym[key] = entry
      end
    end
    return round, gym
  end

  def find_round_unsolved_problems(round)
    puts 'Looking for unsolved problems, this may take some minutes, be patient'
    regex = %r{problem/[A-Z]}
    round_unsolved = []
    round.each do |contest_id, entry|
      url = "http://codeforces.com/contest/#{contest_id}"
      page = @agent.get(url)
      parser = page.parser
      rows = parser.xpath("//tr[@class='rejected-problem']//td//a")
      rows.each do |row|
        matches = regex.match(row.to_html)
        next if matches.nil? || matches.size == 0
        link = "http://codeforces.com/contest/#{contest_id}/#{matches[0]}"
        round_unsolved << link if round_unsolved.size == 0 || link != round_unsolved.last
      end
    end
    round_unsolved
  end

  def find_gym_unsolved_problems(gym)
    puts 'Looking for unsolved problems at gym, this may take some minutes, be patient'
    regex = %r{problem/[A-Z]}
    unsolved = []
    gym.each do |contest_id, entry|
      url = "http://codeforces.com/gym/#{contest_id}"
      page = @agent.get(url)
      parser = page.parser
      rows = parser.xpath("//tr[@class='rejected-problem']//td//a")
      rows.each do |row|
        matches = regex.match(row.to_html)
        next if matches.nil? || matches.size == 0
        link = "http://codeforces.com/gym/#{contest_id}/#{matches[0]}"
        unsolved << link if unsolved.size == 0 || link != unsolved.last
      end
    end
    unsolved
  end
end


print 'Codeforces Handle: '
user = gets.strip

print 'Codeforces Password: '
password = gets.strip

upsolving = Upsolving.new
upsolving.do_login(user, password)
data = upsolving.load_contest_data
round, gym = upsolving.split_contest_data(data)

round_unsolved = upsolving.find_round_unsolved_problems(round)
puts "#{round_unsolved.size} problems to upsolve from rounds"
round_unsolved.each do |problem|
  puts problem
end
puts "\n"

gym_unsolved = upsolving.find_gym_unsolved_problems(gym)
puts "#{gym_unsolved.size} problems to upsolve from gym"
gym_unsolved.each do |problem|
  puts problem
end
puts "\n"
